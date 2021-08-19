package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.reward.RankRewardData;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class RankReward extends Reward
{

	private static final ItemStack ITEM_STACK = new ItemStack(Material.NETHER_STAR);
	private static final ItemStack ITEM_STACK_FAILED = new ItemStack(Material.PAPER);

	private final boolean _canPassLegend;

	public RankReward(RewardRarity rarity, int shardValue, boolean canPassLegend)
	{
		super(rarity, shardValue);

		_canPassLegend = canPassLegend;
	}

	private PermissionGroup getNext(PermissionGroup current)
	{
		PermissionGroup newGroup = null;
		
		switch (current)
		{
			case PLAYER:
				newGroup = PermissionGroup.ULTRA;
				break;
			case ULTRA:
				newGroup = PermissionGroup.HERO;
				break;
			case HERO:
				newGroup = PermissionGroup.LEGEND;
				break;
			case LEGEND:
				newGroup = PermissionGroup.TITAN;
				break;
			case TITAN:
				newGroup = PermissionGroup.ETERNAL;
				break;
			default:
				break;
		}
		
		return newGroup;
	}

	@Override
	public RewardData giveRewardCustom(Player player)
	{
		PermissionGroup group = getNext(CLIENT_MANAGER.Get(player).getPrimaryGroup());
		
		if (group == null)
		{
			return new RewardData(null, getRarity().getColor() + "Rank Upgrade Error", ITEM_STACK_FAILED, getRarity());
		}

		CLIENT_MANAGER.setPrimaryGroup(player, group, () -> {});
		
		return new RankRewardData(getRarity().getColor() + group.getDisplay(false, false, false, true) + " Rank", ITEM_STACK, getRarity(), group);
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		PermissionGroup group = getNext(CLIENT_MANAGER.Get(player).getPrimaryGroup());

		if (group == null)
		{
			return new RewardData(null, getRarity().getColor() + "Rank Upgrade Error", ITEM_STACK_FAILED, getRarity());
		}

		return new RankRewardData(getRarity().getColor() + group.getDisplay(false, false, false, true) + " Rank", ITEM_STACK, getRarity(), group);
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return !CLIENT_MANAGER.Get(player).getPrimaryGroup().inheritsFrom(_canPassLegend ? PermissionGroup.ETERNAL : PermissionGroup.LEGEND);
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof RankReward;
	}
}
