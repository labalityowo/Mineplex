package mineplex.core.lifetimes;

/**
 * A Component defines behavior that can exist within a Lifetime. Components
 * should have no impact upon the game while not active.
 */
public interface Component
{
	/**
	 * Activates the Component, performing any sort of required initialization.
	 * Components may be activated and deactivated multiple times, however a component
	 * will not be activated more than once without subsequent calls to deactivate.
	 */
	void activate();

	/**
	 * Deactivates the Component, disabling any sort of functionality it provides
	 * and performing clean up.  A Component may be subsequently reactivated.
	 */
	void deactivate();
}
