package mineplex.game.clans.message;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ClansMessageManager extends MiniPlugin
{
	private static final Message BLANK_MESSAGE = new Message("", "", 20);

	private HashMap<Player, Message> _playerMessageMap;

	public ClansMessageManager(JavaPlugin plugin)
	{
		super("Message", plugin);

		_playerMessageMap = new HashMap<>();
	}

	@EventHandler
	public void tick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		_playerMessageMap.forEach((player, message) ->{
			message.increment();
			if (message.shouldSend())
			{
				message.send(player);
			}
		});
	}

	public void setMessage(Player player, String title, String description, int ticksBetween, boolean displayNow)
	{
		Message message = new Message(title, description, ticksBetween);
		_playerMessageMap.put(player, message);
		if (displayNow) message.send(player);
	}

	public void removePlayer(Player player)
	{
		removePlayer(player, true);
	}

	public void removePlayer(Player player, boolean sendBlankMessage)
	{
		BLANK_MESSAGE.send(player);
		_playerMessageMap.remove(player);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		removePlayer(event.getPlayer(), false);
	}
}
