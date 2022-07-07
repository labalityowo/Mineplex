package mineplex.game.clans.economy;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GoldPurchaseProcessor implements ConfirmationProcessor
{
	private final Player _player;
	private final int _price;
	private final GoldManager _goldManager;
	private final Runnable _runAfterPurchase;

	public GoldPurchaseProcessor(Player player, int price, GoldManager goldManager, Runnable runAfterPurchase)
	{
		_player = player;
		_price = price;
		_goldManager = goldManager;
		_runAfterPurchase = runAfterPurchase;
	}

	@Override
	public void init(Inventory inventory)
	{
		inventory.setItem(4, new ItemBuilder(ClansCurrency.GOLD.getDisplayMaterial()).setTitle(ClansCurrency.GOLD.getPrefix()).addLore(C.cGray + _price + " " + ClansCurrency.GOLD.getPrefix() + " will be", C.cGray + "deducted from your account balance").build());
	}

	@Override
	public void process(ConfirmationCallback callback) {
		int goldCount = _goldManager.Get(_player).getBalance();

		if (_price > goldCount)
		{
			showResults(callback, TransactionResponse.InsufficientFunds);
		}
		else
		{
			_goldManager.rewardGold(data ->
			{
				if (data)
				{
					showResults(callback, TransactionResponse.Success);
					_player.playSound(_player.getLocation(), Sound.LEVEL_UP, 2f, 1.5f);
				}
				else
				{
					showResults(callback, TransactionResponse.Failed);
				}
			}, _player, -_price, true);

		}
	}

	private void showResults(ConfirmationCallback callback, TransactionResponse response)
	{
		switch (response)
		{
			case Failed:
				callback.reject("There was an error processing your request.");
				break;
			case AlreadyOwns:
				callback.reject("You already own this package.");
				break;
			case InsufficientFunds:
				callback.reject("Your account has insufficient funds.");
				break;
			case Success:
				callback.resolve("Your purchase was successful.");
				_runAfterPurchase.run();
				break;
		}
	}
}