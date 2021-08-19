package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.reward.RewardData;

public class RuneAmplifierReward extends InventoryReward
{

	private static final ItemStack ITEM_STACK = new ItemStack(Material.NETHER_STAR);

	private final int _minutes;

	public RuneAmplifierReward(int minutes, RewardRarity rarity, int shardValue)
	{
		this(minutes, 1, rarity, shardValue);
	}

	public RuneAmplifierReward(int minutes, int amount, RewardRarity rarity, int shardValue)
	{
		this(minutes, amount, amount, rarity, shardValue);
	}

	public RuneAmplifierReward(int minutes, int min, int max, RewardRarity rarity, int shardValue)
	{
		super("Clans Amplifier", "Rune Amplifier " + minutes, "Clans Amplifier", min, max, ITEM_STACK, rarity, shardValue);

		_minutes = minutes;
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + "Clans Amplifier", getRarity().getColor() + _minutes + " minute Clans Amplifier", ITEM_STACK, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof RuneAmplifierReward && ((RuneAmplifierReward) obj)._minutes == _minutes;
	}
	
	@Override
	public int hashCode()
	{
		return Integer.hashCode(_minutes);
	}
}