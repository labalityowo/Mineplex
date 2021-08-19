package mineplex.core.common.shape;

import org.bukkit.Location;

/**
 * Interface used by classes which can display visuals at provided locations.
 */
public interface CosmeticShape
{
	/**
	 * Display a visual at the given location
	 * @param loc The location to display the visual at
	 */
	void display(Location loc);

}
