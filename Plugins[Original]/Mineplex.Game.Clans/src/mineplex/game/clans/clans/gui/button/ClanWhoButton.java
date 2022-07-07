package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.shop.item.IButton;

public class ClanWhoButton implements IButton
{
	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Who)).isCancelled())
			return;
		player.closeInventory();

		JsonMessage message = new JsonMessage(C.cRed + C.Bold + "Click here to lookup a clan").click(ClickEvent.SUGGEST_COMMAND, "/c who ");
		message.sendToPlayer(player);
	}
}
