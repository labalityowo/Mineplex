package mineplex.core.lifetimes;

import mineplex.core.common.util.UtilServer;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A Lifetime that is not thread-safe and is essentially a list of Components.
 * All components are activated in the order that they are registered. All
 * components are deactivated in the reverse order that they are registered.
 * Components may not be registered during invocations of start or end. If a
 * Component throws an exception during activation it will be logged, however the
 * Component will still only be disabled once all other Components are disabled.
 */
public class SimpleLifetime implements Lifetime
{
	protected final List<Component> _components = new ArrayList<>();
	protected boolean _active = false;
	@Override
	public void register(Component component)
	{
		this._components.add(component);
		if (this.isActive())
		{
			try
			{
				component.activate();
			} catch (Exception e)
			{
				e.printStackTrace();
				UtilServer.getPlugin().getLogger().severe("Failed to active component: " + component);
			}
		}
	}

	@Override
	public boolean isActive()
	{
		return _active;
	}

	/**
	 * Starts the SimpleLifetime, activating all components in the order that
	 * they were registered. A SimpleLifetime may be started multiple times,
	 * so long that every invocation of start after the first is preceded by
	 * an invocation of {@link #end()}. If a Component throws an exception
	 * during activation the exception will be logged, however no specific
	 * error-handling mechanism is provided.  The Component will still be
	 * considered active and will be deactivated with all other Components.
	 * @throws IllegalStateException if currently active
	 */
	public void start() throws IllegalStateException
	{
		Validate.isTrue(!_active);
		_active = true;
		for (Component component : _components)
		{
			try
			{
				component.activate();
			} catch (Exception e)
			{
				e.printStackTrace();
				UtilServer.getPlugin().getLogger().severe("Failed to active component: " + component);
			}
		}
	}

	/**
	 * Deactivates all components in the reverse order that they were registered.
	 * Any exception thrown by a Component while being deactivated will be logged,
	 * however no exception handling mechanism is provided.
	 * @throws IllegalStateException if not currently active
	 */
	public void end() throws IllegalStateException
	{
		Validate.isTrue(_active);
		_active = false;
		ListIterator<Component> reverseIterator = _components.listIterator(_components.size());
		while (reverseIterator.hasPrevious())
		{
			Component component = reverseIterator.previous();
			try
			{
				component.deactivate();
			} catch (Exception e)
			{
				e.printStackTrace();
				UtilServer.getPlugin().getLogger().severe("Failed to deactivate component: " + component);
			}
		}
	}
}
