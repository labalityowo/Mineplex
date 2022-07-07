package mineplex.game.clans.clans.cash;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class CashShop extends ShopBase<CashShopManager>
{
	public CashShop(CashShopManager manager)
	{
		super(manager, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), "Online Store");
	}

	@Override
	protected ShopPageBase<CashShopManager, ? extends ShopBase<CashShopManager>> buildPagesFor(Player player)
	{
		return new CashOverviewPage(getPlugin(), this, "Online Store", player);
	}
}