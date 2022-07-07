package mineplex.core.lifetimes;

/**
 * Represents an object that is associated with a specific lifetime.
 * Multiple Lifetimed objects may be associated with the same Lifetime.
 * As a roughly generalized explanation, any time functionality should
 * be enabled(whether its a command, listener, etc.) it should be registered
 * as a Component of a Lifetime.  Any object wishing to enable functionality
 * that is associated with this Lifetimed object should do so with the Lifetime
 * returned by {@link #getLifetime()}.
 */
public interface Lifetimed
{
	/**
	 * Gets the Lifetime associated with this Lifetimed object.
	 * @return non-null Lifetime
	 */
	Lifetime getLifetime();
}
