package mineplex.core.treasure.animation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.animations.reward.CommonRewardAnimation;
import mineplex.core.treasure.animation.animations.reward.LegendaryRewardAnimation;
import mineplex.core.treasure.animation.animations.reward.MythicalRewardAnimation;
import mineplex.core.treasure.animation.animations.reward.RareRewardAnimation;
import mineplex.core.treasure.animation.animations.reward.UncommonRewardAnimation;
import mineplex.core.treasure.types.Treasure;

public abstract class TreasureRewardAnimation extends TreasureAnimation
{

	private static final HologramManager HOLOGRAM_MANAGER = Managers.require(HologramManager.class);
	private static final ItemStack FALL_BACK_ITEM = new ItemStack(Material.PRISMARINE_SHARD);

	public static TreasureRewardAnimation getAnimationFor(Treasure treasure, TreasureLocation treasureLocation, Location location, RewardData rewardData)
	{
		switch (rewardData.getRarity())
		{
			case COMMON:
				return new CommonRewardAnimation(treasure, treasureLocation, location, rewardData);
			case UNCOMMON:
				return new UncommonRewardAnimation(treasure, treasureLocation, location, rewardData);
			case RARE:
				return new RareRewardAnimation(treasure, treasureLocation, location, rewardData);
			case LEGENDARY:
				return new LegendaryRewardAnimation(treasure, treasureLocation, location, rewardData);
			case MYTHICAL:
				return new MythicalRewardAnimation(treasure, treasureLocation, location, rewardData);
		}

		return null;
	}

	private final Location _location;
	private final RewardData _rewardData;

	private Hologram _hologram;

	public TreasureRewardAnimation(Treasure treasure, TreasureLocation treasureLocation, Location location, RewardData rewardData)
	{
		super(treasure, treasureLocation);

		_location = location;
		_rewardData = rewardData;
	}

	protected void createHologramItemPair()
	{
		UtilServer.runSyncLater(this::createHologramItemPair0, 5);
	}

	private void createHologramItemPair0()
	{
		ArmorStand itemHolder = spawnArmourStand(_location.clone().subtract(0, 1.5, 0));
		Item item = spawnItem(_location, _rewardData.getDisplayItem() == null ? FALL_BACK_ITEM : _rewardData.getDisplayItem(), false);
		itemHolder.setPassenger(item);

		List<String> text = new ArrayList<>();

		if (_rewardData.isRewardedShards())
		{
			text.add(C.cAqua + "Duplicate");
			text.add("+" + F.currency(GlobalCurrency.TREASURE_SHARD, _rewardData.getShards()));
			text.add(" ");
		}

		text.add(_rewardData.getHeader());
		text.add(_rewardData.getFriendlyName());

		_hologram = new Hologram(HOLOGRAM_MANAGER, _location.clone().add(0, 0.5, 0), true, text.toArray(new String[0]))
				.start();
	}

	@Override
	public void cleanup()
	{
		super.cleanup();

		if (_hologram != null)
		{
			_hologram.stop();
		}
	}

	public Location getLocation()
	{
		return _location;
	}
}
