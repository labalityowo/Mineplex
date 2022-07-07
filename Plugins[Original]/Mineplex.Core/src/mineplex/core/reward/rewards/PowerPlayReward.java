package mineplex.core.reward.rewards;

import java.time.LocalDate;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.util.UtilServer;
import mineplex.core.powerplayclub.PowerPlayData.SubscriptionDuration;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class PowerPlayReward extends Reward
{
	private CoreClientManager _clientManager;
	private SubscriptionDuration _duration;

	public PowerPlayReward(CoreClientManager clientManager, SubscriptionDuration duration, RewardRarity rarity, int shardValue)
	{
		super(rarity, shardValue);

		_clientManager = clientManager;
		_duration = duration;
	}

	@Override
	public RewardData giveRewardCustom(Player player)
	{
		if (_clientManager.getAccountId(player) == -1)
		{
			return getFakeRewardData(player);
		}
		
		Managers.get(BonusManager.class).getPowerPlayClubRepository().addSubscription(_clientManager.getAccountId(player), LocalDate.now(), _duration.toString().toLowerCase());
		player.setMetadata("GIVEN-PPC-REWARD", new FixedMetadataValue(UtilServer.getPlugin(), System.currentTimeMillis()));
		
		return new RewardData(getRarity().getDarkColor() + "Power Play Subscription", getRarity().getColor() + "1 " + _duration.toString().toLowerCase() + " Power Play Club Subscription", new ItemStack(Material.FIREBALL), getRarity());
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + "Power Play Subscription", getRarity().getColor() + "Power Play Subscription", new ItemStack(Material.FIREBALL), getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return !(Managers.get(BonusManager.class).getPowerPlayClubRepository().getCachedData(player).isSubscribed() || player.hasMetadata("GIVEN-PPC-REWARD"));
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof PowerPlayReward;
	}
}
