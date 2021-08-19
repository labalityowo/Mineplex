package mineplex.core.treasure.animation.animations.reward;

import org.bukkit.Location;
import org.bukkit.Sound;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureRewardAnimation;
import mineplex.core.treasure.types.Treasure;

public class RareRewardAnimation extends TreasureRewardAnimation
{

	private double _radius = 3;
	private double _height = -0.5;

	public RareRewardAnimation(Treasure treasure, TreasureLocation treasureLocation, Location location, RewardData rewardData)
	{
		super(treasure, treasureLocation, location, rewardData);
	}

	@Override
	protected void onStart()
	{
		createHologramItemPair();
		getLocation().getWorld().playSound(getLocation(), Sound.WITHER_SPAWN, 1, 0.5F);
		UtilParticle.PlayParticleToAll(ParticleType.ENCHANTMENT_TABLE, getLocation(), 0.5F, 0.5F, 0.5F, 0.1F, 10, ViewDist.NORMAL);
	}

	@Override
	public void onTick()
	{
		UtilParticle.PlayParticleToAll(ParticleType.PORTAL, getLocation(), 0.5F, 0.5F, 0.5F, 0.1F, 4, ViewDist.NORMAL);

		if (getTicks() % 2 == 0)
		{
			for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 20)
			{
				double x = _radius * Math.cos(theta);
				double z = _radius * Math.sin(theta);

				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getLocation().clone().add(x, _height, z), 0, 0, 0, 0.001F, 1, ViewDist.NORMAL);
			}

			_radius -= 0.1;
			_height += 0.1;

			if (_radius <= 0)
			{
				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, getLocation().clone().add(0, _height, 0), 0, 0, 0, 0.5F, 30, ViewDist.NORMAL);
				setRunning(false);
			}
		}
	}

	@Override
	protected void onFinish()
	{

	}
}
