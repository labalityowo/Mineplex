package nautilus.game.arcade.game.games.minecraftleague.variation.wither.data;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;

import org.bukkit.Location;
import org.bukkit.entity.Wither;
import org.bukkit.util.Vector;

public class PathfinderData 
{
	public Wither Wither;  

	public Location Target = null;
	public Location Location = null;

	public float Pitch = 0;
	public Vector Velocity = new Vector(0,0,0);
	
	//private long _lastReview;

	public PathfinderData(Wither wither, Location target) 
	{
		Wither = wither; 
		UtilEnt.ghost(wither, true, false);
		UtilEnt.vegetate(wither, false);

		Location temp = wither.getLocation();
		temp.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(wither.getLocation(), target)));
		wither.teleport(temp);

		Velocity = wither.getLocation().getDirection().setY(0).normalize();
		Pitch = UtilAlg.GetPitch(wither.getLocation().getDirection());

		Location = wither.getLocation(); 
		//_lastReview = System.currentTimeMillis();
	}

	public void move()
	{
		turn();

		//Speed
		double speed = 0.325 / 2;
		
		Location.add(Velocity.clone().multiply(speed));
		Location.add(0, -Pitch, 0);

		Location.setPitch(-1 * Pitch);
		Location.setYaw(/*180 +*/ UtilAlg.GetYaw(Velocity));
		
		//
		if (!UtilBlock.airFoliage(Location.getBlock()))
		{
			Location.setY(UtilBlock.getHighest(Location.getWorld(), Location.getBlockX(), Location.getBlockZ()).getY());
		}

		Wither.teleport(Location);
	}

	private void turn() 
	{
		//Pitch
		float desiredPitch = UtilAlg.GetPitch(UtilAlg.getTrajectory(Location, Target));
		if (desiredPitch < Pitch)	Pitch = (float)(Pitch - 0.05);
		if (desiredPitch > Pitch)	Pitch = (float)(Pitch + 0.05);
		if (Pitch > 0.5)	Pitch = 0.5f;
		if (Pitch < -0.5)	Pitch = -0.5f;

		//Flat
		Vector desired = UtilAlg.getTrajectory2d(Location, Target);
		desired.subtract(UtilAlg.Normalize(new Vector(Velocity.getX(), 0, Velocity.getZ())));
		desired.multiply(0.2);

		Velocity.add(desired);

		//Speed
		UtilAlg.Normalize(Velocity);			
	}
}