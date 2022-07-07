package mineplex.game.clans.clans.gui;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.page.ClanMainPage;
import mineplex.game.clans.clans.gui.page.ClanWhoPage;

public class ClanShop extends ShopBase<ClansManager>
{
	public ClanShop(ClansManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "ClansShop");
	}

	@Override
	protected ShopPageBase<ClansManager, ? extends ShopBase<ClansManager>> buildPagesFor(Player player)
	{
		return new ClanMainPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	public void openClanWho(Player player, ClanInfo clan)
	{
		openPageForPlayer(player, new ClanWhoPage(getPlugin(), this, getClientManager(), getDonationManager(), player, clan, false));
	}
}
