package mineplex.core.tournament.ui.page;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.tournament.TournamentManager;
import mineplex.core.tournament.ui.TournamentShop;

public class SoloTournamentMenu extends ShopPageBase<TournamentManager, TournamentShop>
{
	public SoloTournamentMenu(TournamentManager plugin, TournamentShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Solo Tournaments", player);
	}

	@Override
	protected void buildPage()
	{
		
	}
}
