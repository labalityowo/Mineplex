package mineplex.core.party.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.spigotmc.CaseInsensitiveMap;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.party.InviteData;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;

/**
 * This class manages invites to parties
 */
public class PartyInviteManager
{
	// Map of Player Name (invitee) to invites (inviter)
	private final Map<String, Set<InviteData>> _activeInvites = new CaseInsensitiveMap<>();

	private final Map<UUID, BukkitTask> _tasks = new HashMap<>();

	private final PartyManager _plugin;

	public PartyInviteManager(PartyManager plugin)
	{
		_plugin = plugin;
	}

	public InviteData getInviteBySender(String invitee, String sender)
	{
		Set<InviteData> map = _activeInvites.get(invitee);

		if (map == null) return null;

		for (InviteData inviteData : map)
		{
			if (inviteData.getInviterName().equalsIgnoreCase(sender))
			{
				return inviteData;
			}
		}

		return null;
	}

	/**
	 * Checks if a caller has an invite by a sender's name
	 *
	 * @param caller
	 * @param sender
	 * @return
	 */
	public boolean hasInviteFrom(Player caller, String sender)
	{
		return getInviteBySender(caller.getName(), sender) != null;
	}

	/**
	 * Checks if a caller has an invite by a party id
	 *
	 * @param caller
	 * @param partyId
	 * @return
	 */
	public boolean hasInviteTo(String caller, UUID partyId)
	{
		Set<InviteData> map = _activeInvites.get(caller);

		if (map == null) return false;

		for (InviteData inviteData : map)
		{
			if (inviteData.getPartyUUID().equals(partyId))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Remove a player's invite to a party by id
	 *
	 * @param caller
	 * @param partyId
	 * @return
	 */
	public InviteData removeInviteTo(String caller, UUID partyId)
	{
		Set<InviteData> map = _activeInvites.get(caller);

		if (map == null) return null;

		Iterator<InviteData> itr = map.iterator();

		while (itr.hasNext())
		{
			InviteData ent = itr.next();
			if (ent.getPartyUUID().equals(partyId))
			{
				itr.remove();
				return ent;
			}
		}

		return null;
	}

	/**
	 * Remove a player's invite to a certain party
	 */
	public InviteData removeInvite(Player invitee, String inviter)
	{
		Set<InviteData> map = _activeInvites.get(invitee.getName());

		if (map == null) return null;

		Iterator<InviteData> itr = map.iterator();

		while (itr.hasNext())
		{
			InviteData ent = itr.next();
			if (ent.getInviterName().equalsIgnoreCase(inviter))
			{
				itr.remove();
				return ent;
			}
		}

		return null;
	}

	/**
	 * Remove all references tied with this player
	 *
	 * @param player The player's UUID
	 */
	public void removeAll(Player player)
	{
		_activeInvites.remove(player.getName());
	}

	/**
	 * Retrieves all invites currently pending for a specific player
	 *
	 * @param player The player
	 * @return All his current pending invites
	 */
	public List<InviteData> getAllInvites(Player player)
	{
		return new ArrayList<>(_activeInvites.getOrDefault(player.getName(), Collections.emptySet()));
	}

	/**
	 * Add a playerUUID to the active invites map
	 *
	 * @param inviterName
	 * @param inviterUUID
	 */
	public void inviteTo(String targetName, UUID targetUUID, String inviterName, UUID inviterUUID, UUID partyId, String server)
	{
		Set<InviteData> map = _activeInvites.computeIfAbsent(targetName, key -> new HashSet<>());
		InviteData inviteData = new InviteData(inviterName, inviterUUID, partyId, server);

		if (map.add(inviteData))
		{
			_tasks.put(targetUUID, _plugin.runSyncLater(new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!hasInviteTo(targetName, partyId))
					{
						cancel();
						return;
					}
					InviteData data = removeInviteTo(targetName, partyId);
					Player possible = Bukkit.getPlayer(targetUUID);
					if (possible != null)
					{
						Party curParty = _plugin.getPartyById(partyId);
						if (curParty != null)
						{
							possible.sendMessage(F.main("Party", "Your invite to " + F.name(inviterName) + "'s party has expired"));
						}
						else
						{
							UtilPlayer.message(possible, F.main("Party", "Your invite to " + F.name(inviterName) + "'s party has expired. Fortunately, that party no longer exists"));
						}
					}
				}
			}, 20 * 60));
		}

		Player player = Bukkit.getPlayer(targetUUID);
		if (player != null)
		{
			Party party = _plugin.getPartyByPlayer(player);
			if (party == null)
			{
				if (server.equals(UtilServer.getServerName()))
				{
					UtilPlayer.message(player, F.main("Party", "You have been invited to " + F.elem(inviterName) + "'s party! You have 60 seconds to reply"));
				}
				else
				{
					UtilPlayer.message(player, F.main("Party", "You have been invited to " + F.elem(inviterName) + "'s party! You have 60 seconds to reply. If you accept, you will be be sent to " + F.elem(server)));
				}
			}
			else
			{
				if (server.equals(UtilServer.getServerName()))
				{
					UtilPlayer.message(player, F.main("Party", "You have been invited to " + F.elem(inviterName) + "'s party! You have 60 seconds to reply. If you accept, you will leave your current party"));
				}
				else
				{
					UtilPlayer.message(player, F.main("Party", "You have been invited to " + F.elem(inviterName) + "'s party! You have 60 seconds to reply. If you accept, you will leave your current party and you will be be sent to " + F.elem(server)));
				}
			}
			sendAcceptOrDeny(player, inviterName);
		}
	}

	/**
	 * Sends a Text Componoent clickable message to a player for easier quick responses to invites
	 *
	 * @param player The player who received the invite
	 * @param arg    The name of the inviting party
	 */
	private void sendAcceptOrDeny(Player player, String arg)
	{
		TextComponent textComponent = new TextComponent(F.main("Party", "Reply: "));

		TextComponent accept = new TextComponent("ACCEPT");
		accept.setColor(ChatColor.GREEN);
		accept.setBold(true);
		accept.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/party cli a " + arg));
		accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
				new TextComponent("Click to join " + F.name(arg) + ChatColor.WHITE + "'s party")
		}));

		textComponent.addExtra(accept);
		textComponent.addExtra(" ");

		TextComponent deny = new TextComponent("DENY");
		deny.setColor(ChatColor.RED);
		deny.setBold(true);
		deny.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/party cli d " + arg));
		deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
				new TextComponent("Click to decline joining " + F.name(arg) + ChatColor.WHITE + "'s party")
		}));

		textComponent.addExtra(deny);
		textComponent.addExtra(" ");

		TextComponent view = new TextComponent("VIEW");
		view.setColor(ChatColor.YELLOW);
		view.setBold(true);
		view.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/party cli is"));
		view.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
				new TextComponent("Click to view all pending invites")
		}));

		textComponent.addExtra(view);

		player.spigot().sendMessage(textComponent);
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 10.0F);
	}
}
