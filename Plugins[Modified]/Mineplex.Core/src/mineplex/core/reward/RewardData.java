package mineplex.core.reward;

import org.bukkit.inventory.ItemStack;

import mineplex.core.treasure.reward.RewardRarity;

public class RewardData
{
	private final String _header;
	private final String _friendlyName;
	private final ItemStack _displayItem;
	private final RewardRarity _rarity;
	private final boolean _rewardedShards;
	private int _shards;

	public RewardData(String header, String friendlyName, ItemStack displayItem, RewardRarity rarity, int shards)
	{
		_header = header;
		_friendlyName = friendlyName;
		_displayItem = displayItem;
		_rarity = rarity;
		_rewardedShards = true;
		_shards = shards;
	}

	public RewardData(String header, String friendlyName, ItemStack displayItem, RewardRarity rarity)
	{
		_header = header;
		_friendlyName = friendlyName;
		_displayItem = displayItem;
		_rarity = rarity;
		_rewardedShards = false;
		_shards = 0;
	}

	public String getHeader()
	{
		return _header;
	}

	public String getFriendlyName()
	{
		return _friendlyName;
	}

	public ItemStack getDisplayItem()
	{
		return _displayItem;
	}

	public RewardRarity getRarity()
	{
		return _rarity;
	}

	public boolean isRewardedShards()
	{
		return _rewardedShards;
	}

	public int getShards()
	{
		return _shards;
	}

	public void setShards(int value)
	{
		_shards = value;
	}
}
