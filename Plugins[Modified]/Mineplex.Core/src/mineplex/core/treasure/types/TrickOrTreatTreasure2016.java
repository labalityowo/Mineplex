package mineplex.core.treasure.types;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.gadget.gadgets.morph.MorphPumpkinKing;
import mineplex.core.gadget.gadgets.mount.types.MountZombie;
import mineplex.core.pet.PetType;
import mineplex.core.reward.RewardType;
import mineplex.core.reward.rewards.GameAmplifierReward;
import mineplex.core.reward.rewards.RuneAmplifierReward;
import mineplex.core.reward.rewards.SpinTicketReward;
import mineplex.core.reward.rewards.UnknownPackageReward;
import mineplex.core.treasure.animation.animations.TrickOrTreatChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class TrickOrTreatTreasure2016 extends Treasure
{

	public TrickOrTreatTreasure2016()
	{
		super(TreasureType.TRICK_OR_TREAT);

		setAnimation(treasureLocation -> new TrickOrTreatChestAnimation(this, treasureLocation));
		setRewards(RewardType.TRICK_OR_TREAT_CHEST);
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
		addChestReward(TreasureType.MYTHICAL, rarity, 1, 3, 50);
		addChestReward(TreasureType.ILLUMINATED, rarity, 1, 1, 30);
		addGadgetReward(getGadget(MountZombie.class), rarity, 25);
		addPetReward(PetType.ZOMBIE, rarity, 10);
		addGadgetReward(getGadget(MorphPumpkinKing.class), rarity, 5);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
		addRankReward(rarity, true, 100);
	}
}
