package mineplex.gemhunters.loot.rewards;

import mineplex.core.server.util.TransactionResponse;
import mineplex.gemhunters.util.SlackRewardBot;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.donation.DonationManager;
import mineplex.core.donation.Donor;

public class LootGadgetReward extends LootItemReward
{

	private final DonationManager _donation;
	
	private final String _gadget;
	
	public LootGadgetReward(long cashOutDelay, ItemStack itemStack, String gadget)
	{
		super(gadget, cashOutDelay, itemStack);
		
		_donation = Managers.require(DonationManager.class);
		_gadget = gadget;
	}

	@Override
	public void onCollectItem()
	{
		
	}

	@Override
	public void onSuccessful()
	{
		Donor donor = _donation.Get(_player);
		
		if (donor.ownsUnknownSalesPackage(_gadget))
		{
			_donation.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, _player, "Earned", (int) (500 + 1000 * Math.random()), success -> SlackRewardBot.logReward(_player, this, (success ? "Success" : "Failure") + " (Shard Dupe)"));
		}
		else
		{
			_donation.purchaseUnknownSalesPackage(_player, _gadget, GlobalCurrency.TREASURE_SHARD, 0, true, transaction -> SlackRewardBot.logReward(_player, this, transaction == TransactionResponse.Success ? "Success" : "Failure"));
		}
	}

	@Override
	public void onDeath()
	{
		
	}

}
