package mineplex.core.lifetimes;

/**
 * A Lifetime represents a duration for which a collection of Components
 * will be active. While Lifetime does contain a method for registering
 * instantiated Components, individual Lifetimes may have unique
 * strategies for creating Components and activating them.  Lifetime
 * doesn't provide any guarantee of Component activation or deactivation
 * order.  Implementations of Lifetime, however, may.
 * <p />
 * Lifetime doesn't provide mechanisms for beginning or ending a Lifetime.
 * This is provided by the various implementations of Lifetime, as it varies
 * between the implementations and is functionality that most consumers of
 * Lifetimes will not need.
 */
public interface Lifetime
{
	/**
	 * Registers the provided component with this Lifetime. If the Lifetime
	 * is currently active, then the Component will be immediately activated.
	 * @param component the component to register
	 */
	void register(Component component);
	/**
	 * Gets whether the Lifetime is currently active.
	 * @return true if the Lifetime is active
	 */
	boolean isActive();
}
