package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.core.shop.item.IButton;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class ClanCreateButton implements IButton
{
	public ClanCreateButton()
	{

	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Create)).isCancelled())
		{
			return;
		}
		
		TextComponent message = new TextComponent("Click Here to create a Clan!");
		message.setColor(ChatColor.AQUA);
		message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/c create "));
		player.spigot().sendMessage(message);
	}
}
