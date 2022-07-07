package mineplex.core.tournament.ui;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.tournament.TournamentManager;
import mineplex.core.tournament.ui.page.MainMenu;
import org.bukkit.entity.Player;

public class TournamentShop extends ShopBase<TournamentManager>
{
	public TournamentShop(TournamentManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Tournaments");
	}

	@Override
	protected ShopPageBase<TournamentManager, ? extends ShopBase<TournamentManager>> buildPagesFor(Player player)
	{
		return new MainMenu(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
