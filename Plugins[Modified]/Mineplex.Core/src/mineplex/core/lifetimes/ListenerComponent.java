package mineplex.core.lifetimes;

import mineplex.core.common.util.UtilServer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * A convenience class for Components that are a Listener.  ListenerComponent
 * can either be used as a wrapper class for a specific Listener, or can be
 * extended by another Component to provide event registration.
 */
public class ListenerComponent implements Component, Listener
{
	private final Listener _listener;

	/**
	 * Creates a ListenerComponent that registers the provided Listener when
	 * activated and unregisters it when deactivated.  The newly created
	 * ListenerComponent will not be registered as a Listener.  When a
	 * ListenerComponent is created with this constructor, it is effectively
	 * a wrapper to bind a Listener to a specific lifetime.
	 *
	 * @param listener non-null listener to wrap
	 * @throws IllegalArgumentException if listener is null
	 */
	public ListenerComponent(Listener listener) throws IllegalArgumentException
	{
		Validate.notNull(listener);
		_listener = listener;
	}

	/**
	 * Creates a ListenerComponent that registers itself when activated and
	 * unregisters itself when deactivated.
	 */
	public ListenerComponent()
	{
		_listener = this;
	}

	@Override
	public void activate()
	{
		Bukkit.getPluginManager().registerEvents(_listener, UtilServer.getPlugin());
	}

	@Override
	public void deactivate()
	{
		HandlerList.unregisterAll(_listener);
	}
}
