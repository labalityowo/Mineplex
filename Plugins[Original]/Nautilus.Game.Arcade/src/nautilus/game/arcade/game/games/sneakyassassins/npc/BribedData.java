package nautilus.game.arcade.game.games.sneakyassassins.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BribedData
{
	public Player Player;
	public Location LastLocation;
	public long LastTime;
	
	public BribedData(Player player)
	{
		Player = player;
	}
}
