package mineplex.core.reward;

import org.bukkit.inventory.ItemStack;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.treasure.reward.RewardRarity;

public class RankRewardData extends RewardData
{

	private final PermissionGroup _rank;
	
	public RankRewardData(String friendlyName, ItemStack displayItem, RewardRarity rarity, PermissionGroup rank)
	{
		super(C.cRed + "Rank Upgrade", friendlyName, displayItem, rarity);

		_rank = rank;
	}
	
	public PermissionGroup getWonRank()
	{
		return _rank;
	}
}