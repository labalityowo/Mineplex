package mineplex.game.clans.core.repository;

import org.bukkit.Location;

import mineplex.game.clans.core.ClaimLocation;

public class ClanTerritory
{
	public ClanTerritory(ClaimLocation loc, String owner, boolean safe)
	{
		ClaimLocation = loc;
		Owner = owner;
		Safe = safe;
	}

	public boolean Safe;
	public String Owner = "";
	public ClaimLocation ClaimLocation;

	public boolean isSafe(Location location)
	{
		if (Owner.equals("Spawn"))
		{
			return location.getY() > 190;
		}
		
		return Safe;
	}
}