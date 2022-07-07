package nautilus.game.arcade.kit.perks.data;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;

public class ChickenMissileData
{
	public Player Player;
	public Entity Chicken;
	public Vector Direction;
	public long Time;
	
	public Location Last;
	
	public ChickenMissileData(Player player, Entity chicken)
	{
		Player = player;
		Chicken = chicken;
		Direction = player.getLocation().getDirection().multiply(0.6);
		Time = System.currentTimeMillis();
	}
	
	public boolean HasHitBlock()
	{
		Location current = Chicken.getLocation();

		//Not First Run
		if (Last != null && UtilMath.offsetSquared(Last, current) < 0.2 || UtilEnt.isGrounded(Chicken))
		{
			return true;
		}
		
		Last = current;
		return false;
	}
}
