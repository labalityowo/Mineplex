package mineplex.core.reward.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class UnknownPackageReward extends Reward
{

	private final String _header, _displayName, _packageName;
	private final ItemStack _itemStack;

	public UnknownPackageReward(String header, String displayName, String packageName, ItemStack item, RewardRarity rarity, int shardValue)
	{
		super(rarity, shardValue);

		_header = header;
		_displayName = displayName;
		_packageName = packageName;
		_itemStack = item;
	}

	@Override
	protected RewardData giveRewardCustom(Player player)
	{
		DONATION_MANAGER.purchaseUnknownSalesPackage(player, _packageName, GlobalCurrency.TREASURE_SHARD, 0, true, null);

		return getFakeRewardData(player);
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + _header, getRarity().getColor() + _displayName, _itemStack, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return !DONATION_MANAGER.Get(player).ownsUnknownSalesPackage(_packageName) || INVENTORY_MANAGER.Get(player).getItemCount(_packageName) > 0;
	}
}
