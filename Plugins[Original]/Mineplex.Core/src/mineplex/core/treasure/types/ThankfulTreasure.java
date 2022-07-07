package mineplex.core.treasure.types;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.gadget.gadgets.mount.types.MountChicken;
import mineplex.core.gadget.gadgets.mount.types.MountCake;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.reward.rewards.SpinTicketReward;
import mineplex.core.reward.rewards.GameAmplifierReward;
import mineplex.core.reward.rewards.RuneAmplifierReward;
import mineplex.core.reward.rewards.ChestReward;
import mineplex.core.reward.rewards.UnknownPackageReward;
import mineplex.core.reward.rewards.PowerPlayReward;
import mineplex.core.treasure.animation.animations.ThankfulChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.powerplayclub.PowerPlayData.SubscriptionDuration;

public class ThankfulTreasure extends Treasure
{

	public ThankfulTreasure()
	{
		super(TreasureType.THANKFUL);

		setAnimation(treasureLocation -> new ThankfulChestAnimation(this, treasureLocation));
		setRewards(RewardType.THANKFUL_CHEST);
		setRewardsPerChest(4);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		addReward(new SpinTicketReward(1, 3, rarity, 0), 150);
		addReward(new GameAmplifierReward(1, 2, rarity, 0), 150);
		addReward(new RuneAmplifierReward(20, 1, 3, rarity, 0), 120);
		addReward(new UnknownPackageReward("Clan Banner Access", "Wear/Place Clan Banner", "Clan Banner Usage", new ItemStack(Material.BANNER), rarity, 0), 110);
		addChestReward(TreasureType.OLD, rarity, 1, 5, 150);
		addChestReward(TreasureType.ANCIENT, rarity, 1, 5, 80);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addReward(new ChestReward(TreasureType.MYTHICAL, 1, 3, rarity, 0), 50);
		addReward(new ChestReward(TreasureType.ILLUMINATED, 1, 1, rarity, 0), 30);
		addGadgetReward(getGadget(MountChicken.class), rarity, 5);
		addGadgetReward(getGadget(MountCake.class), rarity, 10);
		addPetReward(PetType.VILLAGER, rarity, 10);
		addPetReward(PetType.PIG_ZOMBIE, rarity, 10);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
		addRankReward(rarity, true, 100);
		addReward(new PowerPlayReward(Managers.get(CoreClientManager.class), SubscriptionDuration.MONTH, rarity, 0), 90);
	}
}
