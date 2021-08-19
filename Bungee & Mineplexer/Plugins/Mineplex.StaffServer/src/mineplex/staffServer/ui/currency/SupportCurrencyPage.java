package mineplex.staffServer.ui.currency;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.shop.item.ShopItem;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportCurrencyPage extends SupportPage
{
	private static final int[] CURRENCY_DEMONINATIONS = new int[] { 5000, 15000, 25000, 75000 };

	public SupportCurrencyPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "Currency");

		buildPage();
	}

	private void buildCurrency(GlobalCurrency currency, int rowOffset)
	{
		int slot = getSlotIndex(rowOffset, 1);

		for (int i = 0; i < 4; i++)
		{
			int amount = CURRENCY_DEMONINATIONS[i];

			addButton(slot,
					new ShopItem(currency.getDisplayMaterial(), currency.getColor() + C.Bold + currency.getString(amount, false), new String[] {C.mBody + "Click to award " + currency.getString(amount) }, i + 1, false, true),
					(p, c) -> getPlugin().getDonationManager().rewardCurrency(currency, _target, "Support - " + getPlayer().getName(), amount, true, (success) ->
					{
						if (success)
						{
							playSuccess();
							message("You awarded " + currency.getString(amount) + C.mBody + " to " + C.cYellow + _target.getName());

							// Update their counts in the player skull
							refresh();
						}
						else
						{
							playFail();
							message("Unable to award currency at this time. Please try again later.");
						}
					}));

			slot += 2;
		}
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		buildCurrency(GlobalCurrency.GEM, 2);
		buildCurrency(GlobalCurrency.TREASURE_SHARD, 4);
	}
}
