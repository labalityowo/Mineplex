package mineplex.gemhunters.loot.rewards;

import mineplex.gemhunters.util.SlackRewardBot;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.donation.DonationManager;

public class LootShardReward extends LootItemReward
{

	private final DonationManager _donation;
	
	private final int _amount;
	
	public LootShardReward(long cashOutDelay, ItemStack itemStack, int amount)
	{
		super("Shard", cashOutDelay, itemStack);
		
		_donation = Managers.require(DonationManager.class);
		_amount = amount;
	}

	@Override
	public void onCollectItem()
	{
		
	}

	@Override
	public void onSuccessful()
	{
		_donation.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, _player, "Earned", _amount, success -> SlackRewardBot.logReward(_player, this, success ? "Success" : "Failure"));
	}

	@Override
	public void onDeath()
	{
		
	}

}
