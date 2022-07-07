package mineplex.core.treasure.animation.animations.reward;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureRewardAnimation;
import mineplex.core.treasure.types.Treasure;

public class MythicalRewardAnimation extends TreasureRewardAnimation
{

	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.STAR)
			.withColor(Color.RED)
			.withFade(Color.MAROON)
			.withFlicker()
			.build();

	public MythicalRewardAnimation(Treasure treasure, TreasureLocation treasureLocation, Location location, RewardData rewardData)
	{
		super(treasure, treasureLocation, location, rewardData);
	}

	@Override
	protected void onStart()
	{
		createHologramItemPair();
		getLocation().getWorld().playSound(getLocation(), Sound.ENDERDRAGON_DEATH, 1, 0.5F);
		UtilParticle.PlayParticleToAll(ParticleType.LAVA, getLocation(), 0.5F, 0.5F, 0.5F, 0.1F, 10, ViewDist.NORMAL);
	}

	@Override
	public void onTick()
	{
		if (getTicks() % 5 == 0)
		{
			for (Player player : getLocation().getWorld().getPlayers())
			{
				player.setPlayerTime(player.getPlayerTime() + 12000, false);
			}
		}

		if (getTicks() % 2 == 0)
		{
			Location random = UtilAlg.getRandomLocation(getLocation(), 5, 0, 5);

			if (Math.random() < 0.3)
			{
				random.getWorld().strikeLightningEffect(random);
			}
			UtilFirework.launchFirework(random, FIREWORK_EFFECT, null, UtilMath.r(3) + 1);
		}

		if (getTicks() > 80)
		{
			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{
		for (Player player : getLocation().getWorld().getPlayers())
		{
			player.resetPlayerTime();
		}
	}
}
