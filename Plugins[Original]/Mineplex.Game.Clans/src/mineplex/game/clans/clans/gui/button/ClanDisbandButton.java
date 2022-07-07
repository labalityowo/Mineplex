package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;

public class ClanDisbandButton extends ClanButton
{
	public ClanDisbandButton(ClanShop shop, ClansManager clansManager, Player player, ClanInfo clanInfo, ClanRole clanRole)
	{
		super(shop, clansManager, player, clanInfo, clanRole);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Disband)).isCancelled())
			return;
		getPlayer().closeInventory();
		getClansManager().getClanUtility().delete(getPlayer());
	}
}
