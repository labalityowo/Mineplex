package nautilus.game.arcade.kit.perks.data;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;

import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

public class HomingSheepData
{
	
	private static final int MAX_LIFE_TICKS = 300;
	private static final int TARGET_RADIUS = 2;
	private static final float VELOCITY = 0.36F;
	
	public Player Shooter;
	public Player Target;
	public Sheep Sheep;
	
	private int _colorTick = 0;
	
	public HomingSheepData(Player shooter, Player target, Sheep sheep)
	{
		Shooter = shooter;
		Target = target;
		Sheep = sheep;
	}

	public boolean update()
	{
		if (!Sheep.isValid() || !Target.isValid() || UtilPlayer.isSpectator(Target))
		{
			return true;
		}
		
		if (Sheep.getTicksLived() > MAX_LIFE_TICKS)
		{
			return true;
		}
		
		if (UtilMath.offset(Sheep.getLocation(), Target.getEyeLocation()) < TARGET_RADIUS)
		{
			return true;
		}
		
		Sheep.setVelocity(UtilAlg.getTrajectory(Sheep.getLocation(), Target.getEyeLocation()).multiply(VELOCITY));
		
		Sheep.getWorld().playSound(Sheep.getLocation(), Sound.SHEEP_IDLE, 1.5f, 1.5f);
		
		if (_colorTick == 0) 	Sheep.setColor(DyeColor.RED);
		if (_colorTick == 1) 	Sheep.setColor(DyeColor.YELLOW);
		if (_colorTick == 2) 	Sheep.setColor(DyeColor.LIME);
		if (_colorTick == 3) 	Sheep.setColor(DyeColor.LIGHT_BLUE);
		if (_colorTick == 4) 	Sheep.setColor(DyeColor.PURPLE);
		
		_colorTick = (_colorTick + 1) % 5;
		
		return false;
	}
}
