package mineplex.core.treasure.animation.animations.reward;

import org.bukkit.Location;

import mineplex.core.reward.RewardData;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureRewardAnimation;
import mineplex.core.treasure.types.Treasure;

public class CommonRewardAnimation extends TreasureRewardAnimation
{

	public CommonRewardAnimation(Treasure treasure, TreasureLocation treasureLocation, Location location, RewardData rewardData)
	{
		super(treasure, treasureLocation, location, rewardData);
	}

	@Override
	protected void onStart()
	{
		createHologramItemPair();
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
