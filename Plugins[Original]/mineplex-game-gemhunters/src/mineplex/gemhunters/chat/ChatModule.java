package mineplex.gemhunters.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.chat.Chat;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.progression.ProgressionModule;

/**
 * This module handles player chat.
 */
@ReflectivelyCreateMiniPlugin
public class ChatModule extends MiniPlugin
{

	private final CoreClientManager _clientManager;
	private final Chat _chat;
	private final EconomyModule _economy;
	private final PartyManager _party;
	private final ProgressionModule _progression;

	private ChatModule()
	{
		super("Chat");
		
		_clientManager = require(CoreClientManager.class);
		_chat = require(Chat.class);
		_economy = require(EconomyModule.class);
		_party = require(PartyManager.class);
		_progression = require(ProgressionModule.class);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerJoin(PlayerJoinEvent event)
	{
		event.setJoinMessage(F.sys("Join", event.getPlayer().getName()));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit(PlayerQuitEvent event)
	{
		event.setQuitMessage(F.sys("Quit", event.getPlayer().getName()));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void chat(AsyncPlayerChatEvent event)
	{
		// Checks if the player has been muted/chat is silenced etc...
		if (event.isCancelled())
		{
			return;
		}
		
		Player player = event.getPlayer();
		String playerName = player.getName();

		PermissionGroup group = _clientManager.Get(player).getRealOrDisguisedPrimaryGroup();
		String rankString = group.getDisplay(false, false, false, false).isEmpty() ? "" : group.getDisplay(true, true, true, false);
		
		// Create a message that follows the rest of the network's chat format
		String message = (rankString + " " + C.cYellow + playerName + " ");
		
		// We will handle the broadcast
		event.setCancelled(true);
	
		if (event.getMessage().charAt(0) == '@')
		{
			Party party = _party.getPartyByPlayer(player);
			if (party != null)
			{
				if (event.getMessage().length() > 1)
				{
					event.setMessage(event.getMessage().substring(1, event.getMessage().length()).trim());
					message = C.cDPurpleB + "Party " + C.cWhiteB + playerName + " " + C.cPurple;

                    event.getRecipients().removeIf(other -> !party.getMembers().contains(other));
				}
				else
				{
					player.sendMessage(F.main("Party", "Where's the message?"));
				}
			}
		}
		else
		{
			message = _progression.getTitle(_economy.getGems(player)).getTitle() + " " + message + C.cWhite;
		}
		
		message += _chat.filterMessage(player, event.getMessage());
		
		message = message.trim();
		
		for (Player other : event.getRecipients())
		{
			other.sendMessage(message);
		}
	}
}