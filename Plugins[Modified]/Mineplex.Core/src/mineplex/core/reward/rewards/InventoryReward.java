package mineplex.core.reward.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class InventoryReward extends Reward
{

	private final ItemStack _itemStack;
	private final String _name;
	private final String _packageName;
	private final String _header;
	private final int _minAmount;
	private final int _maxAmount;

	public InventoryReward(String name, String packageName, String header, ItemStack itemStack, RewardRarity rarity, int shardValue)
	{
		this(name, packageName, header, 1, itemStack, rarity, shardValue);
	}

	public InventoryReward(String name, String packageName, String header, int amount, ItemStack itemStack, RewardRarity rarity, int shardValue)
	{
		this(name, packageName, header, amount, amount, itemStack, rarity, shardValue);
	}

	public InventoryReward(String name, String packageName, String header, int minAmount, int maxAmount, ItemStack itemStack, RewardRarity rarity, int shardValue)
	{
		super(rarity, shardValue);

		_name = name;
		_packageName = packageName;
		_header = header;
		_minAmount = minAmount;
		_maxAmount = maxAmount;
		_itemStack = itemStack;
	}

	@Override
	public RewardData giveRewardCustom(Player player)
	{
		int amountToGive;

		if (_minAmount != _maxAmount)
		{
			amountToGive = RANDOM.nextInt(_maxAmount - _minAmount) + _minAmount;
		}
		else
		{
			amountToGive = _minAmount;
		}

		INVENTORY_MANAGER.addItemToInventory(player, _packageName, amountToGive);

		if (amountToGive == 1)
		{
			return getFakeRewardData(player);
		}

		return new RewardData(getRarity().getDarkColor() + _header, getRarity().getColor() + amountToGive + " " + _name, _itemStack, getRarity());
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + _header, getRarity().getColor() + _name, _itemStack, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return true;
	}

	protected String getPackageName()
	{
		return _packageName;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof InventoryReward && ((InventoryReward) obj).getPackageName().equals(_packageName);
	}
	
	@Override
	public int hashCode()
	{
		return _packageName.hashCode();
	}
}