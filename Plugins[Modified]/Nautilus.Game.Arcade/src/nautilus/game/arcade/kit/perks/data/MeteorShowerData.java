package nautilus.game.arcade.kit.perks.data;

import mineplex.core.common.util.UtilTime;
import net.minecraft.server.v1_8_R3.EntityFireball;
import net.minecraft.server.v1_8_R3.EntityLargeFireball;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLargeFireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MeteorShowerData
{
		
	public Player Shooter;
	public Location Target;
	public long Time;
	private long _maxTime;

	public MeteorShowerData(Player shooter, Location target, long maxTime)
	{
		Shooter = shooter;
		Target = target;
		Time = System.currentTimeMillis();
		_maxTime = maxTime;
	}	

	public boolean update()
	{
		if (UtilTime.elapsed(Time, _maxTime))
			return true;

		LargeFireball ball = Target.getWorld().spawn(Target.clone().add(Math.random() * 24 - 12, 32 + Math.random() * 16, Math.random() * 24 - 12), LargeFireball.class);

		EntityLargeFireball eFireball = ((CraftLargeFireball) ball).getHandle();
		eFireball.dirX = (Math.random()-0.5)*0.02;
		eFireball.dirY = -0.2 - 0.05 * Math.random();
		eFireball.dirZ = (Math.random()-0.5)*0.02;
		
		ball.setShooter(Shooter);
		ball.setYield(2.2f);
		ball.setBounce(false);

		return false;
	}
}
