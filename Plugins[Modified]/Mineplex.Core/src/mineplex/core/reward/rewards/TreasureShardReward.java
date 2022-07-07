package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class TreasureShardReward extends Reward
{

	private static final ItemStack ITEM_STACK = new ItemStack(Material.PRISMARINE_SHARD);

	private Reward _otherReward;
	private final int _shardsMin;
	private final int _shardsMax;

	public TreasureShardReward(Reward otherReward, RewardRarity rarity)
	{
		super(rarity, 0);

		_otherReward = otherReward;

		_shardsMin = (int) (otherReward.getShardValue() + (Math.random() * otherReward.getShardValue() / 2.0));
		_shardsMax = _shardsMin;
	}

	public TreasureShardReward(int shards)
	{
		this(shards, shards, RewardRarity.UNCOMMON);
	}

	public TreasureShardReward(int min, int max, RewardRarity rarity)
	{
		super(rarity, 0);

		_shardsMin = min;
		_shardsMax = max;
	}

	public Reward getOtherReward()
	{
		return _otherReward;
	}

	@Override
	public RewardData giveRewardCustom(Player player)
	{
		RewardData rewardData;
		int shards;

		if (_shardsMin == _shardsMax)
		{
			shards = _shardsMin;
		}
		else
		{
			shards = UtilMath.rRange(_shardsMin, _shardsMax);;
		}

		if (_otherReward != null)
		{
			RewardData fakeData = _otherReward.getFakeRewardData(player);
			rewardData = new RewardData(fakeData.getHeader(), fakeData.getFriendlyName(), fakeData.getDisplayItem(), fakeData.getRarity(), shards);
		}
		else
		{
			rewardData = new RewardData(null, getRarity().getColor() + shards + " Treasure Shards", ITEM_STACK, getRarity());
		}

		CoreClient client = CLIENT_MANAGER.Get(player);

		// Give shards 5 seconds later for better effect
		UtilServer.runSyncLater(() -> DONATION_MANAGER.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, client, "Treasure", shards), 100);

		return rewardData;
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(null, getRarity().getColor() + "Treasure Shards", ITEM_STACK, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof TreasureShardReward;
	}

}

