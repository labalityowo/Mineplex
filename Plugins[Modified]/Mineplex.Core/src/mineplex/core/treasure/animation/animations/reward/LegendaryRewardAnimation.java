package mineplex.core.treasure.animation.animations.reward;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureRewardAnimation;
import mineplex.core.treasure.types.Treasure;

public class LegendaryRewardAnimation extends TreasureRewardAnimation
{

	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.BALL_LARGE)
			.withColor(Color.LIME)
			.withFade(Color.GREEN)
			.withFlicker()
			.build();
	private static final ItemStack ITEM_STACK = new ItemStack(Material.EMERALD);

	public LegendaryRewardAnimation(Treasure treasure, TreasureLocation treasureLocation, Location location, RewardData rewardData)
	{
		super(treasure, treasureLocation, location, rewardData);
	}

	@Override
	protected void onStart()
	{
		createHologramItemPair();
		getLocation().getWorld().playSound(getLocation(), Sound.ENDERDRAGON_DEATH, 1, 1);
		getLocation().getWorld().strikeLightningEffect(getLocation());
	}

	@Override
	public void onTick()
	{
		if (getTicks() % 8 == 0)
		{
			UtilFirework.playFirework(getLocation(), FIREWORK_EFFECT);

			for (int i = 0; i < 3; i++)
			{
				Item item = spawnItem(getLocation(), ITEM_STACK, true);
				item.setVelocity(new Vector(Math.random() - 0.5, Math.random(), Math.random() - 0.5));
			}
		}

		if (getTicks() > 80)
		{
			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{

	}
}
