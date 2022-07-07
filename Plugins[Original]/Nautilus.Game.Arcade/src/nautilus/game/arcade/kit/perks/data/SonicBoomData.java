package nautilus.game.arcade.kit.perks.data;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;

public class SonicBoomData
{
	public Player Shooter;
	public Location Location;
	public Vector Direction;
	public long Time;
	
	public SonicBoomData(Player player)
	{
		Shooter = player;
		Location = player.getEyeLocation().add(player.getLocation().getDirection());
		Direction = player.getLocation().getDirection();
		Time = System.currentTimeMillis();
	}
}
