package mineplex.minecraft.game.classcombat.Skill;

import org.bukkit.Location;

/*
 * Determines whether a location is acceptable for an action to be performed
 */
public interface LocationFilter
{
	LocationFilter ACCEPT_ALL = location -> true;

	/*
	 * Check if the given location is acceptable
	 * @param location The location to check
	 * @return Whether the location is valid, or invalid
	 */
	boolean accept(Location location);
}
