package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;

public class ClanJoinButton implements IButton
{
	private ClansManager _clansManager;
	private ClanInfo _clanInfo;

	public ClanJoinButton(ClansManager clansManager, ClanInfo clanInfo)
	{
		_clansManager = clansManager;
		_clanInfo = clanInfo;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Join)).isCancelled())
			return;
		player.closeInventory();
		_clansManager.getClanUtility().join(player, _clanInfo);
	}
}
