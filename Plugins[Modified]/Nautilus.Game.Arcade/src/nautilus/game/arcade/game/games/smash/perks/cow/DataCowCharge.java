package nautilus.game.arcade.game.games.smash.perks.cow;

import org.bukkit.Location;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DataCowCharge
{
	public long Time;
	public Player Player;
	public Vector Direction;
	
	public Cow Cow;
	
	public Location LastLoc;
	public long LastMoveTime;
	
	public DataCowCharge(Player player, Cow cow)
	{
		Time = System.currentTimeMillis();
		
		Player = player;
		
		Direction = player.getLocation().getDirection();
		Direction.setY(0);	
		Direction.normalize();
		Direction.multiply(0.75);
		Direction.setY(-0.2);
		
		Cow = cow;
		
		LastLoc = Cow.getLocation();
		LastMoveTime = System.currentTimeMillis();
	}
}
