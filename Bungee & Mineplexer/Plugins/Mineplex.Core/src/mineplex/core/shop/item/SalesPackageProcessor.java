package mineplex.core.shop.item;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationProcessor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static mineplex.core.Managers.require;

public class SalesPackageProcessor implements ConfirmationProcessor
{
	private final CoreClientManager _clientManager = require(CoreClientManager.class);

	private final GlobalCurrency _currencyType;
	private final SalesPackageBase _salesItem;
	private final DonationManager _donationManager;
	private final Player _player;
	private final Runnable _runAfterPurchase;

	public SalesPackageProcessor(Player player, GlobalCurrency currencyType, SalesPackageBase salesItem, DonationManager donationManager, Runnable runAfterPurchase)
	{
		_currencyType = currencyType;
		_salesItem = salesItem;
		_donationManager = donationManager;
		_player = player;
		_runAfterPurchase = runAfterPurchase;
	}

	@Override
	public void init(Inventory inventory)
	{
		inventory.setItem(4, new ItemBuilder(_currencyType.getDisplayMaterial()).setTitle(_currencyType.getPrefix()).addLore(C.cGray + _salesItem.getCost(_currencyType) + " " + _currencyType.getPrefix() + " will be", C.cGray + "deducted from your account balance").build());
	}

	@Override
	public void process(ConfirmationCallback callback)
	{
		CoreClient client = _clientManager.Get(_player);
		if (_salesItem.isKnown())
		{
			_donationManager.purchaseKnownSalesPackage(client, _salesItem.getSalesPackageId(), response -> showResults(callback, response));
		}
		else
		{
			_donationManager.purchaseUnknownSalesPackage(client, _salesItem.getName(), _currencyType, _salesItem.getCost(_currencyType), _salesItem.oneTimePurchase(), response -> showResults(callback, response));
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

				if (_runAfterPurchase != null)
					_runAfterPurchase.run();

				break;
		}
	}
}
