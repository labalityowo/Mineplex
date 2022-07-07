package mineplex.core.treasure.animation.animations.reward;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureRewardAnimation;
import mineplex.core.treasure.types.Treasure;

public class UncommonRewardAnimation extends TreasureRewardAnimation
{

	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.BURST)
			.withColor(Color.AQUA)
			.withFade(Color.WHITE)
			.withFlicker()
			.build();

	public UncommonRewardAnimation(Treasure treasure, TreasureLocation treasureLocation, Location location, RewardData rewardData)
	{
		super(treasure, treasureLocation, location, rewardData);
	}

	@Override
	protected void onStart()
	{
		createHologramItemPair();
		UtilParticle.PlayParticleToAll(ParticleType.HEART, getLocation(), 0.25F, 0.25F, 0.25F, 0.1F, 4, ViewDist.NORMAL);
		UtilFirework.playFirework(getLocation(), FIREWORK_EFFECT);
		setRunning(false);
	}

	@Override
	public void onTick()
	{

	}

	@Override
	protected void onFinish()
	{

	}
}
