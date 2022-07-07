package mineplex.core.progression.data;

import java.util.function.Consumer;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import mineplex.core.progression.math.Calculations;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationProcessor;

public class KitUpgradeProcessor implements ConfirmationProcessor
{
	private KitProgressionManager _manager;
	private Player _player;
	private ProgressiveKit _kit;
	private int _upgradeLevel;

	public KitUpgradeProcessor(KitProgressionManager manager, Player player, ProgressiveKit kit, int upgradeLevel)
	{
		_manager = manager;
		_player = player;
		_kit = kit;
		_upgradeLevel = upgradeLevel;
	}

	@Override
	public void init(Inventory inventory)
	{
	}

	@Override
	public void process(ConfirmationCallback callback)
	{
		String packageName = _kit.getInternalName() + "." + _upgradeLevel;
		int cost = Calculations.getGemsCost(_upgradeLevel);
		if (!_kit.usesXp())
		{
			cost = Calculations.getGemsCostXpLess(_upgradeLevel);
		}
		
		Consumer<TransactionResponse> handler = response ->
		{
			if (response == TransactionResponse.Success)
			{
				_kit.upgrade(_upgradeLevel, _player.getUniqueId());

				_player.playSound(_player.getLocation(), Sound.CAT_MEOW, 5.0f, 1.0f);
				_player.sendMessage(F.main("Kit Progression", "Purchased upgrades for " + _kit.getDisplayName() + " level " + _upgradeLevel));

				callback.resolve("Success! You now own this upgrade!");
			}
			else if (response == TransactionResponse.InsufficientFunds)
			{
				callback.reject("Insufficient funds!");
			}
			else
			{
				callback.reject("There was an error processing your transaction. Try again later");
			}
		};
		
		// Use UnknownPackages for this right now as it handles overspending gems properly
		if (_kit.crownsEnabled())
		{
			_manager.getDonationManager().purchaseUnknownSalesPackageCrown(_player, packageName, cost, false, handler);
		}
		else
		{
			_manager.getDonationManager().purchaseUnknownSalesPackage(_player, packageName, GlobalCurrency.GEM, cost, false, handler);
		}
	}
}