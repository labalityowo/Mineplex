package nautilus.game.arcade.game.games.dragonescape;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilWorld;

import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.util.Vector;

public class DragonEscapeData 
{

	public final DragonEscape Host;
	public final EnderDragon Dragon;

	public Location Target;
	public Location Location;

	private float Pitch;
	public Vector Velocity;

	DragonEscapeData(DragonEscape host, EnderDragon dragon, Location target)
	{
		Host = host; 

		Dragon = dragon; 
		UtilEnt.ghost(Dragon, true, false);

		Location temp = dragon.getLocation();
		temp.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(dragon.getLocation(), target)));
		dragon.teleport(temp);

		Velocity = dragon.getLocation().getDirection().setY(0).normalize();
		Pitch = UtilAlg.GetPitch(dragon.getLocation().getDirection());

		Location = dragon.getLocation(); 
	}

	public void Move()
	{
		Turn();
		
		double speed = 0.2 * Host.GetSpeedMult();
		
		Location.add(Velocity.clone().multiply(speed));
		Location.add(0, -Pitch, 0);

		Location.setPitch(-1 * Pitch);
		Location.setYaw(180 + UtilAlg.GetYaw(Velocity));

		UtilEnt.setPosition(Dragon, Location);
	}

	private void Turn() 
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
