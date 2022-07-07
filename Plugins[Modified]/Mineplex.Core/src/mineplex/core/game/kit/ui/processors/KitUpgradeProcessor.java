package mineplex.core.game.kit.ui.processors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationProcessor;

public class KitUpgradeProcessor implements ConfirmationProcessor
{

	private final MineplexGameManager _manager;
	private final Player _player;
	private final int _cost;
	private final Runnable _runAfterPurchase;

	public KitUpgradeProcessor(MineplexGameManager manager, Player player, int cost, Runnable runAfterPurchase)
	{
		_manager = manager;
		_player = player;
		_cost = cost;
		_runAfterPurchase = runAfterPurchase;
	}

	@Override
	public void init(Inventory inventory)
	{
	}

	@Override
	public void process(ConfirmationCallback callback)
	{
		_manager.getDonationManager().rewardCurrencyUntilSuccess(GlobalCurrency.GEM, _player, "Kit Upgrade Purchase", -_cost, success ->
		{
			if (success)
			{
				callback.resolve("Successfully upgraded your kit!");

				if (_runAfterPurchase != null)
				{
					_runAfterPurchase.run();
				}
			}
			else
			{
				callback.reject("There was an error processing your request.");
			}
		});
	}
}
