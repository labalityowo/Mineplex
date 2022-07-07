package nautilus.game.arcade.game.games.evolution.evolve;

import org.bukkit.Location;

public class PlatformToken
{
	/**
	 * @author Mysticate
	 * August 8, 2015
	 */
	
	public final Location Platform;
	public final Location Viewing; /**This was when I was using a spectate packet. Will be used again once I can have teh armorstand : >>>>>>>>*/
	public final Location Store;
	
	public PlatformToken(Location platform, Location viewing, Location store)
	{
		Platform = platform.clone();
		Viewing = viewing.clone();
		Store = store.clone();
	}
}
