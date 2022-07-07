package nautilus.game.minekart.shop;

import org.bukkit.entity.Player;

import nautilus.game.minekart.KartFactory;
import nautilus.game.minekart.shop.page.KartPage;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.CurrencyType;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class KartShop extends ShopBase<KartFactory>
{
	public KartShop(KartFactory plugin, CoreClientManager clientManager, DonationManager donationManger, CurrencyType...currencyTypes)
	{
		super(plugin, clientManager, donationManger, "Kart Shop", currencyTypes);
	}

	@Override
	protected ShopPageBase<KartFactory, ? extends ShopBase<KartFactory>> BuildPagesFor(Player player)
	{
		return new KartPage(Plugin, ClientManager, DonationManager, this, player);
	}
}
