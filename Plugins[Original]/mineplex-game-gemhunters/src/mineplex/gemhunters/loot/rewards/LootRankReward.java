package mineplex.gemhunters.loot.rewards;

import java.util.concurrent.TimeUnit;

import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.donation.DonationManager;
import mineplex.gemhunters.util.SlackRewardBot;

public class LootRankReward extends LootItemReward
{
	private static final long CASH_OUT_DELAY = TimeUnit.MINUTES.toMillis(15);
	private static final int CONSOLATION_PRICE = 10000;
	
	private final CoreClientManager _clientManager;
	private final DonationManager _donation;

	public LootRankReward(ItemStack itemStack)
	{
		super("Rank", CASH_OUT_DELAY, itemStack);

		_clientManager = Managers.require(CoreClientManager.class);
		_donation = Managers.require(DonationManager.class);
	}

	@Override
	public void onCollectItem() {}

	@Override
	public void onSuccessful()
	{
		CoreClient client = _clientManager.Get(_player);
		PermissionGroup group = client.getPrimaryGroup();
		PermissionGroup newGroup = null;

		// I could have done this so it runs off the order of the Rank enum,
		// however knowing some people that might get changed so I'm just going
		// to hard code what you get.

		switch (group)
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
		
		// A suitable rank could not be found.
		if (newGroup == null)
		{
			_player.sendMessage(F.main("Loot", "You already have eternal ( You are lucky :) ). So instead you can have " + CONSOLATION_PRICE + " shards."));
			_donation.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, _player, "Earned", CONSOLATION_PRICE, success -> SlackRewardBot.logReward(_player, this, (success ? "Success" : "Failure") + " (Shard Dupe)"));
			return;
		}
		
		final String status = newGroup.name();
		
		_clientManager.setPrimaryGroup(_player, newGroup, () -> SlackRewardBot.logReward(_player, this, status));
	}

	@Override
	public void onDeath() {}
}