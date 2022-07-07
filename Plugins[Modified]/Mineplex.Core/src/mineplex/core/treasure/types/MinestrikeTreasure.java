package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.gamemodifiers.minestrike.MineStrikeSkin;
import mineplex.core.reward.RewardType;
import mineplex.core.reward.rewards.GadgetReward;
import mineplex.core.treasure.animation.animations.MinestrikeChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class MinestrikeTreasure extends Treasure
{

	public MinestrikeTreasure()
	{
		super(TreasureType.MINESTRIKE);

		setAnimation(treasureLocation -> new MinestrikeChestAnimation(this, treasureLocation));
		setRewards(RewardType.MINESTRIKE_CHEST);
		setRewardsPerChest(2);
		setPurchasable(10000);
		purchasableFromStore();
		enabledByDefault();
		allowDuplicates();
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addMineStrikeSkinReward(MineStrikeSkin.SSG_08_Blood_in_the_Water, rarity, 150, 1000);
		addMineStrikeSkinReward(MineStrikeSkin.Galil_AR_Eco, rarity, 150, 1000);
		addMineStrikeSkinReward(MineStrikeSkin.Nova_Koi, rarity, 100, 1000);
		addMineStrikeSkinReward(MineStrikeSkin.Knife_M9_Bayonette_Fade, rarity, 30, 2500);
		addMineStrikeSkinReward(MineStrikeSkin.PP_Bizon_Streak, rarity, 150, 1000);
		addMineStrikeSkinReward(MineStrikeSkin.CZ75_Auto_Tigris, rarity, 100, 1000);
		addMineStrikeSkinReward(MineStrikeSkin.XM1014_Tranquility, rarity, 100, 1000);
		addMineStrikeSkinReward(MineStrikeSkin.Desert_Eagle_Golden_Gun, rarity, 30, 2500);
		addMineStrikeSkinReward(MineStrikeSkin.P90_Asiimov, rarity, 100, 1000);
		addMineStrikeSkinReward(MineStrikeSkin.SG553_Pulse, rarity, 100, 1000);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addMineStrikeSkinReward(MineStrikeSkin.Desert_Eagle_Blaze, rarity, 100, 5000);
		addMineStrikeSkinReward(MineStrikeSkin.Glock_18_Fade, rarity, 100, 5000);
		addMineStrikeSkinReward(MineStrikeSkin.P250_Muertos, rarity, 100, 5000);
		addMineStrikeSkinReward(MineStrikeSkin.AK_47_Vulcan, rarity, 100, 5000);
		addMineStrikeSkinReward(MineStrikeSkin.Knife_Counter_Terrorist_Sword, rarity, 50, 6500);
		addMineStrikeSkinReward(MineStrikeSkin.Knife_Terrorist_Sword, rarity, 50, 6500);
		addMineStrikeSkinReward(MineStrikeSkin.Knife_M9_Bayonette_Glass, rarity, 50, 6500);
		addMineStrikeSkinReward(MineStrikeSkin.AK_47_Guardian, rarity, 10, 7500);
		addMineStrikeSkinReward(MineStrikeSkin.AWP_Asiimov, rarity, 100, 5000);
		addMineStrikeSkinReward(MineStrikeSkin.FAMAS_Pulse, rarity, 100, 5000);
		addMineStrikeSkinReward(MineStrikeSkin.XM1014_Pig_Gun, rarity, 10, 7500);
		addMineStrikeSkinReward(MineStrikeSkin.M4A4_Enderman, rarity, 10, 7500);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}

	private GadgetReward addMineStrikeSkinReward(MineStrikeSkin skin, RewardRarity rarity, int weight, int shards)
	{
		return addGadgetReward(GADGET_MANAGER.getGameCosmeticManager().getGadgetFrom(skin.getSkinName()), rarity, weight, shards);
	}
}
