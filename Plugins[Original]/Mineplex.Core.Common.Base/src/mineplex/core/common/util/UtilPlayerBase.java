package mineplex.core.common.util;

import java.util.LinkedList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.events.PlayerMessageEvent;
import org.bukkit.Bukkit;

public class UtilPlayerBase
{

	public static void message(Entity client, LinkedList<String> messageList)
	{
		message(client, messageList, false);
	}

	public static void message(Entity client, String message)
	{
		message(client, message, false);
	}

	public static void message(Entity client, LinkedList<String> messageList, boolean wiki)
	{
		for (String curMessage : messageList)
		{
			message(client, curMessage, wiki);
		}
	}

	public static void message(Entity client, String message, boolean wiki)
	{
		if (client == null)
			return;

		if (!(client instanceof Player))
			return;

		/*
        if (wiki)
        	message = UtilWiki.link(message);
		 */
		
		PlayerMessageEvent event = new PlayerMessageEvent((Player) client, message);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		
		((Player) client).sendMessage(message);
	}

	public static void messageSearchOnlineResult(Player caller, String player, int matchCount)
	{
		message(caller, F.main("Online Player Search", "" + C.mCount + matchCount + C.mBody + " matches for [" + C.mElem + player + C.mBody + "]."));
	}

	public static Player searchOnline(Player caller, String player, boolean inform)
	{
		LinkedList<Player> matchList = new LinkedList<Player>();

		for (Player cur : Bukkit.getOnlinePlayers())
		{
			if (cur.getName().equalsIgnoreCase(player))
				return cur;
			
			if (cur.getName().toLowerCase().contains(player.toLowerCase()))
				matchList.add(cur);
		}

		// No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform)
				return null;

			// Inform
			messageSearchOnlineResult(caller, player, matchList.size());

			if (matchList.size() > 0)
			{
				String matchString = "";
				for (Player cur : matchList)
					matchString += F.elem(cur.getName()) + ", ";
				if (matchString.length() > 1)
					matchString = matchString.substring(0, matchString.length() - 2);

				message(caller,
						F.main("Online Player Search", "" + C.mBody + "Matches [" + C.mElem + matchString + C.mBody + "]."));
			}

			return null;
		}

		return matchList.get(0);
	}

}
