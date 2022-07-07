package mineplex.core.game.kit.ui.processors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationProcessor;

public class KitPackageProcessor implements ConfirmationProcessor
{

	private final MineplexGameManager _manager;
	private final GameKit _kit;
	private final Player _player;
	private final Runnable _runAfterPurchase;

	public KitPackageProcessor(MineplexGameManager manager, GameKit kit, Player player, Runnable runAfterPurchase)
	{
		_manager = manager;
		_kit = kit;
		_player = player;
		_runAfterPurchase = runAfterPurchase;
	}

	@Override
	public void init(Inventory inventory)
	{
		String lore = GlobalCurrency.GEM.getString(_kit.getCost()) + C.cGray + " will be deducted from your account.";

		inventory.setItem(4, new ItemBuilder(_kit.getEntityData().getInHand())
				.setTitle(_kit.getFormattedName())
				.addLore(UtilText.splitLineToArray(lore, LineFormat.LORE))
				.build());
	}

	@Override
	public void process(ConfirmationCallback callback)
	{
		_manager.getDonationManager().rewardCurrencyUntilSuccess(GlobalCurrency.GEM, _player, "Kit Purchase", -_kit.getCost(), success ->
		{
			if (success)
			{
				callback.resolve("Successfully unlocked " + _kit.getDisplayName());

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
