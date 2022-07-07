package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;
import mineplex.game.clans.clans.gui.page.ClanMainPage;

public class ClanMemeberButton extends ClanButton
{
	private ClanMainPage _page;
	private String _other;

	public ClanMemeberButton(ClanShop shop, ClansManager clansManager, Player player, ClanInfo clanInfo, ClanRole clanRole, ClanMainPage page, String other)
	{
		super(shop, clansManager, player, clanInfo, clanRole);
		_other = other;
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Member)).isCancelled())
			return;
		if (clickType == ClickType.LEFT)
		{
			// Promote
			getClansManager().getClanUtility().promote(getPlayer(), _other);
			_page.refresh();
		}
		else if (clickType == ClickType.RIGHT)
		{
			// Demote
			getClansManager().getClanUtility().demote(getPlayer(), _other);
			_page.refresh();
		}
		else if (clickType == ClickType.SHIFT_RIGHT)
		{
			// Kick
			getClansManager().getClanUtility().kick(getPlayer(), _other);
			_page.refresh();
		}
	}
}
