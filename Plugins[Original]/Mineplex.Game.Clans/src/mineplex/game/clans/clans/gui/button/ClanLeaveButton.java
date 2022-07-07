package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;

public class ClanLeaveButton extends ClanButton
{
	public ClanLeaveButton(ClanShop shop, ClansManager clansManager, Player player, ClanInfo clanInfo, ClanRole clanRole)
	{
		super(shop, clansManager, player, clanInfo, clanRole);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if(clickType.equals(ClickType.SHIFT_RIGHT)) //disband
		{
			if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Disband)).isCancelled())
				return;

			getPlayer().closeInventory();
			getClansManager().getClanUtility().delete(getPlayer());

		}else if(clickType.equals(ClickType.SHIFT_LEFT)) //leave
		{
			if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Leave)).isCancelled())
				return;

			getPlayer().closeInventory();
			getClansManager().getClanUtility().leave(getPlayer());
		}

	}
}
