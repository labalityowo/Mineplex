package nautilus.game.arcade.game.games.moba.general;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.moba.Moba;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BetaManager implements Listener
{

	private static final TextComponent MESSAGE;

	static
	{
		MESSAGE = new TextComponent("You can suggest improvements for the game on our Trello by clicking here!");
		MESSAGE.setColor(ChatColor.AQUA);
		MESSAGE.setClickEvent(new ClickEvent(Action.OPEN_URL, "https://trello.com/b/MrxWVhlI/mineplex-heroes-of-gwen-feedback-update"));
		MESSAGE.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to open the Trello board").color(ChatColor.YELLOW).create()));
	}

	private final Moba _host;

	public BetaManager(Moba host)
	{
		_host = host;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.MIN_02 || _host.GetState() != GameState.Recruit)
		{
			return;
		}

		Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(MESSAGE));
	}
}
