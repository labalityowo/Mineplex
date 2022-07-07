package mineplex.core.giveaway.redis;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class GiveawayMessageHandler implements CommandCallback
{
	private HashMap<String, Long> _cooldownMap;

	public GiveawayMessageHandler()
	{
		_cooldownMap = new HashMap<String, Long>();
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof GiveawayMessage)
		{
			GiveawayMessage message = ((GiveawayMessage) command);

			// %p - player name
			String headerText = message.getGiveawayHeader();
			String chatMessage = message.getGiveawayMessage().replaceAll("%p", message.getPlayerName());

			// Chat Colors
			headerText = ChatColor.translateAlternateColorCodes('&', headerText);
			chatMessage = ChatColor.translateAlternateColorCodes('&', chatMessage);

			UtilTextMiddle.display(headerText, chatMessage, 20, 80, 20);
			for (Player player : UtilServer.getPlayers())
			{
				player.playSound(player.getEyeLocation(), Sound.AMBIENCE_CAVE, 1, 1);
			}
		}
	}
}
