package mineplex.core.lifetimes;

import com.google.common.base.Preconditions;
import mineplex.core.common.util.UtilServer;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A PhasedLifetime is a lifetime that is composed of several
 * smaller lifetimes, referred to as phases.  This class is provided
 * in order to support a system in which Components may exist within multiple
 * Lifetimes. PhasedLifetime is not thread-safe.
 * <p />
 * Registering a Component will register it for the entirety of this Lifetime,
 * unless registered with {@link #register(Component, Iterable)}. Special behavior
 * is provided for instances of {@link PhasedComponent}. See {@link #register(Component,
 * Iterable)} for more information. Components registered using {@link
 * #register(Component)} are registered for the entire duration of the Lifetime.
 */
public class PhasedLifetime<T> implements Lifetime
{
	private final Map<T, List<RegisteredComponent>> _phases = new HashMap<>();
	private final List<Component> _global = new ArrayList<>();
	private boolean _active = false;
	private T _current;

	/**
	 * Registers the Component for all phases within the provided Iterable,
	 * and creates a Lifetime that is active during all those phases, and
	 * therefore identical to when the provided Component is active. If a change
	 * occurs from a Phase that the Component is active in to another phase that
	 * the component is active in, it will not be disabled. When this
	 * Lifetime ends, all Lifetimes created by this Lifetime also end, as all
	 * phases are considered over.
	 * <p />
	 * If the Component is an instance of PhasedComponent, then any phase change
	 * in which one of the phases is within the provided Iterable of phases will
	 * result in an invocation of {@link PhasedComponent#setPhase(Object)}. This
	 * should not be used as a mechanism to detect when the component is being
	 * disabled, but rather as a means to provide specific behavior when that
	 * phase change occurs. If a phase change drastically changes the behavior
	 * of a Component such that not all functionality is active for some phases
	 * that a Component is registered for, you should consider refactoring that
	 * Component into two separate Components.
	 * <p />
	 * As an example, assume that we have a PhasedLifetime with phases A-F,
	 * and a PhasedComponent is registered for phases A, B, and E. The chain
	 * of events would be as followed(italic indicates an event call to the
	 * PhasedComponent, an activation, or deactivation).
	 * <ul>
	 *     <li>Lifetime started with a value of A</li>
	 *     <li><i>Component is activated</i></li>
	 *     <li><i>setPhase is called with a value of A</i></li>
	 *     <li>Phase is set to B</li>
	 *     <li><i>setPhase is called with a value of B</i></li>
	 *     <li>Phase is set to C</li>
	 *     <li><i>setPhase is called with a value of C</i></li>
	 *     <li><i>Component is deactivated</i></li>
	 *     <li>Phase is set to D</li>
	 *     <li>Phase is set to E</li>
	 *     <li><i>Component is activated</i></li>
	 *     <li><i>setPhase is called with a value of E</i></li>
	 *     <li>Phase is set to F</li>
	 *     <li><i>setPhase is called with a value of F</i></li>
	 *     <li><i>Component is deactivated</i></li>
	 *     <li>Lifetime ends</li>
	 * </ul>
	 * <p />
	 * If phases contains no elements, then the Component will not be
	 * registered.
	 * @param component non-null Component being registered
	 * @param phases non-null Iterable of phases to register the Component for
	 * @return a Lifetime corresponding to when the Component is active
	 * @throws IllegalArgumentException if component or phases is null
	 */
	public Lifetime register(Component component, Iterable<T> phases) throws IllegalArgumentException {
		Validate.notNull(component, "Component cannot be null");
		Validate.notNull(phases, "Phases cannot be null");
		RegisteredComponent rComponent = new RegisteredComponent(component);
		for (T phase : phases)
		{
			_phases.computeIfAbsent(phase, (p) -> new ArrayList<>()).add(rComponent);
			if (Objects.equals(phase, _current))
			{
				rComponent.start();
				if (component instanceof PhasedComponent)
				{
					((PhasedComponent<T>) component).setPhase(phase);
				}
			}
		}
		return rComponent;
	}

	/**
	 * Starts the Lifetime, activating all components that are active for
	 * the entire lifetime, and then activating all components that are part
	 * of the provided phase.
	 * @param phase non-null phase to start
	 * @throws IllegalArgumentException if phase is null
	 * @throws IllegalStateException if the Lifetime is currently active
	 */
	public void start(T phase) throws IllegalArgumentException, IllegalStateException
	{
		Validate.notNull(phase, "phase cannot be null");
		Preconditions.checkState(!_active, "Lifetime already started");
		_active = true;
		_global.forEach(PhasedLifetime::active);
		setPhase(phase);
	}

	/**
	 * Ends the Lifetime, deactivating all components in the current phase
	 * and then deactivating all components that are active for the entire Lifetime.
	 * A Lifetime may be subsequently reactivated after it has ended.
	 * @throws IllegalStateException if the lifetime isn't active
	 */
	public void end() throws IllegalStateException
	{
		Preconditions.checkState(_active, "Lifetime not active");
		List<RegisteredComponent> toDisable = _phases.get(getPhase());
		if (toDisable != null)
		{
			toDisable.forEach(RegisteredComponent::end);
		}
		_global.forEach(PhasedLifetime::deactive);
		_active = false;
		_current = null;
	}

	/**
	 * Sets the current phase to the provided value, activating components
	 * that are active in the phase and not currently active, and deactiving
	 * components that were previously active and are not registered for the
	 * provided phase.
	 * @param phase non-null
	 * @throws IllegalStateException if the Lifetime isn't active
	 * @throws IllegalArgumentException if the phase equals the current phase
	 *     or is null
	 */
	public void setPhase(T phase) throws IllegalStateException, IllegalArgumentException
	{
		Preconditions.checkState(_active, "Lifetime not active");
		Validate.isTrue(!Objects.equals(phase, _current), "Can't set the phase to the current phase");
		Validate.notNull(phase, "the phase cannot be null");
		T oldPhase = getPhase();
		_current = phase;
		List<RegisteredComponent> old = _phases.get(oldPhase);
		List<RegisteredComponent> nextPhase = _phases.get(phase);

		for (Component c : _global)
		{
			if (c instanceof PhasedComponent)
			{
				((PhasedComponent<T>) c).setPhase(phase);
			}
		}
		// Disable components that were active in the last phase but not in the next phase.
		if (old != null)
		{
			List<RegisteredComponent> toDisable = new ArrayList<>();
			toDisable.addAll(old);
			// Components that are in the next phase shouldn't be disabled.
			if (nextPhase != null)
			{
				toDisable.removeAll(nextPhase);
			}

			// Ensure that all old ones get a setPhase call before disabling them.
			for (RegisteredComponent r : toDisable)
			{
				if (r.getComponent() instanceof PhasedComponent)
				{
					((PhasedComponent<T>) r.getComponent()).setPhase(phase);
				}
			}
			toDisable.forEach(RegisteredComponent::end);
		}
		if (nextPhase != null)
		{
			// New but not old
			List<RegisteredComponent> toActivate = new ArrayList<>();
			toActivate.addAll(nextPhase);
			// Ensure that all components from last phase don't end up getting activated again.
			if (old != null)
			{
				toActivate.removeAll(old);
			}
			// Start all the new ones
			toActivate.forEach(RegisteredComponent::start);
			// Give every remaining component a call to setPhase
			for (RegisteredComponent r : nextPhase)
			{
				if (r.getComponent() instanceof PhasedComponent)
				{
					((PhasedComponent<T>) r.getComponent()).setPhase(phase);
				}
			}
		}
	}

	private static void active(Component component)
	{
		try
		{
			component.activate();
		} catch (Exception e)
		{
			e.printStackTrace();
			UtilServer.getPlugin().getLogger().severe("Failed to activate component: " + component);
		}
	}
	private static void deactive(Component component)
	{
		try
		{
			component.deactivate();
		} catch (Exception e)
		{
			e.printStackTrace();
			UtilServer.getPlugin().getLogger().severe("Failed to deactivate component: " + component);
		}
	}
	/**
	 * Gets the current Phase. If this PhasedLifetime isn't active then
	 * the current phase is null.
	 * @return the current phase, null if not active
	 */
	public T getPhase()
	{
		return _current;
	}

	/**
	 * {@inheritDoc}
	 * If the Component is an instance of PhasedComponent, it will receive
	 * notifications of phase changes prior to any components registered
	 * with specific phases.  Global components will also be deactivated last.
	 * @param component the component to register
	 */
	@Override
	public void register(Component component)
	{
		_global.add(component);
		if (this.isActive())
		{
			component.activate();
		}
	}

	@Override
	public boolean isActive()
	{
		return _active;
	}

	private static class RegisteredComponent extends SimpleLifetime implements Lifetime {
		private Component _component;

		public RegisteredComponent(Component component)
		{
			this._component = component;
		}

		public Component getComponent()
		{
			return _component;
		}

		@Override
		public void start() throws IllegalStateException
		{
			PhasedLifetime.active(_component);
			super.start();
		}

		@Override
		public void end() throws IllegalStateException
		{
			super.end();
			PhasedLifetime.deactive(_component);
		}
	}
}
