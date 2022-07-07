package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.donation.DonationManager;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;
import mineplex.game.clans.clans.gui.page.ClanInvitePage;

public class ClanInviteButton extends ClanButton
{
	private DonationManager _donationManager;

	public ClanInviteButton(ClanShop shop, ClansManager clansManager, DonationManager donationManager, Player player, ClanInfo clanInfo, ClanRole clanRole)
	{
		super(shop, clansManager, player, clanInfo, clanRole);
		_donationManager = donationManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Invite)).isCancelled())
			return;
		getShop().openPageForPlayer(getPlayer(), new ClanInvitePage(getClansManager(), getShop(), getClansManager().getClientManager(), _donationManager, player));
	}
}
