package mineplex.core.reward;

import java.util.Random;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.Callback;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.treasure.reward.RewardRarity;

public abstract class Reward
{

	protected static final CoreClientManager CLIENT_MANAGER = Managers.require(CoreClientManager.class);
	protected static final DonationManager DONATION_MANAGER = Managers.require(DonationManager.class);
	protected static final InventoryManager INVENTORY_MANAGER = Managers.require(InventoryManager.class);
	protected static final Random RANDOM = new Random();

	private RewardRarity _rarity;
	private int _shardValue;
	private boolean _requiresCallback;

	public Reward(RewardRarity rarity, int shardValue)
	{
		this(rarity, shardValue, false);
	}

	public Reward(RewardRarity rarity, int shardValue, boolean requiresCallback)
	{
		_rarity = rarity;
		_shardValue = shardValue;
		_requiresCallback = requiresCallback;
	}

	public final void giveReward(Player player, Callback<RewardData> rewardDataCallback)
	{
		if (_requiresCallback)
		{
			giveRewardCallback(player, rewardDataCallback);
		}
		else
		{
			rewardDataCallback.run(giveRewardCustom(player));
		}
	}

	protected void giveRewardCallback(Player player, Callback<RewardData> rewardDataCallback)
	{
		// do nothing
	}

	protected abstract RewardData giveRewardCustom(Player player);

	public abstract RewardData getFakeRewardData(Player player);

	public abstract boolean canGiveReward(Player player);

	public RewardRarity getRarity()
	{
		return _rarity;
	}

	public int getShardValue()
	{
		return _shardValue;
	}
}
