package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.boosters.BoosterManager;
import mineplex.core.treasure.reward.RewardRarity;

public class GameAmplifierReward extends InventoryReward
{

	private static final ItemStack ITEM_STACK = new ItemStack(Material.EMERALD);

	public GameAmplifierReward(RewardRarity rarity, int shardValue)
	{
		this(1, rarity, shardValue);
	}

	public GameAmplifierReward(int amount, RewardRarity rarity, int shardValue)
	{
		this(amount, amount, rarity, shardValue);
	}

	public GameAmplifierReward(int min, int max, RewardRarity rarity, int shardValue)
	{
		super("Game Amplifier", BoosterManager.BOOSTER_ITEM, "Game Amplifier", min, max, ITEM_STACK, rarity, shardValue);
	}

}
