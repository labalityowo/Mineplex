package mineplex.core.reward.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class GadgetReward extends Reward
{

	private final Gadget _gadget;
	private final ItemStack _itemStack;

	public GadgetReward(Gadget gadget, RewardRarity rarity, int shardValue)
	{
		super(rarity, shardValue);

		_gadget = gadget;

		if (_gadget.hasDisplayItem())
		{
			_itemStack = _gadget.getDisplayItem();
		}
		else
		{
			_itemStack = new ItemBuilder(_gadget.getDisplayMaterial(), _gadget.getDisplayData())
				.build();
		}
	}

	public Gadget getGadget()
	{
		return _gadget;
	}

	@Override
	protected RewardData giveRewardCustom(Player player)
	{
		DONATION_MANAGER.purchaseUnknownSalesPackage(player, _gadget.getName(), GlobalCurrency.TREASURE_SHARD, 0, true, null);

		return getFakeRewardData(player);
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + _gadget.getGadgetType().getCategoryType(), getRarity().getColor() + _gadget.getName(), _itemStack, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return !_gadget.ownsGadget(player);
	}
}
