package nautilus.game.minekart.menu;

import mineplex.core.account.CoreClientManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

import nautilus.game.minekart.KartFactory;
import nautilus.game.minekart.gp.GPManager;
import nautilus.game.minekart.shop.KartItem;

import org.bukkit.entity.Player;

public class KartMenu extends ShopBase<KartFactory>
{
	private GPManager _gpManager;
	
	public KartMenu(KartFactory plugin, CoreClientManager clientManager, mineplex.core.donation.DonationManager donationManager, GPManager gpManager)
	{
		super(plugin, clientManager, donationManager, "Kart Select");
		
		_gpManager = gpManager;
	}

	@Override
	protected ShopPageBase<KartFactory, ? extends ShopBase<KartFactory>> BuildPagesFor(Player player)
	{
		return new KartPage(Plugin, ClientManager, DonationManager, this, _gpManager, player);
	}

	public void SelectKart(Player player, KartItem kartItem)
	{
		_gpManager.SelectKart(player, kartItem.GetKartType());
	}
}
