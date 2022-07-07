package mineplex.core.party;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.jsonchat.ChildJsonMessage;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Menu;
import mineplex.core.message.MessageManager;
import mineplex.core.party.command.PartyCommand;
import mineplex.core.party.constants.PartyRemoveReason;
import mineplex.core.party.manager.PartyInviteManager;
import mineplex.core.party.manager.PartyJoinManager;
import mineplex.core.party.manager.PartyRedisManager;
import mineplex.core.party.rediscommands.PartyCrossServerInviteAccept;
import mineplex.core.party.rediscommands.PartyCrossServerInviteCommand;
import mineplex.core.party.rediscommands.PartyCrossServerInviteDeny;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerCommandManager;

public class PartyManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		PARTY_COMMAND,
	}

	/**
	 * The item given to a player in his hotbar to manage the Parties via the new UI.
	 */
	public static final ItemStack INTERFACE_ITEM = new ItemBuilder(Material.NAME_TAG)
			.setTitle(C.cGreen + "Parties")
			.build();

	/**
	 * The slot to which it goes in.
	 */
	public static final int INTERFACE_SLOT = 5;

	//Managers
	private final Portal _portal;
	private final CoreClientManager _clientManager;
	private final PreferencesManager _preferencesManager;
	private final PartyRedisManager _redisManager;
	private final PartyInviteManager _inviteManager;
	private final PartyJoinManager _joinManager;

	private final MessageManager _messageManager;

	/**
	 * This local instance's name
	 */
	private final String _serverName;

	private final Map<UUID, Party> _partiesById = new HashMap<>();
	private final Map<UUID, Party> _partiesByPlayer = new HashMap<>();

	private final Region _region;

	/**
	 * Maps UUID of player to UUID of party that they're supposed to join
	 */
	private final Map<UUID, UUID> _pendingJoinMap = new HashMap<>();


	public PartyManager()
	{
		super("Parties");
		_portal = require(Portal.class);
		_clientManager = require(CoreClientManager.class);
		_preferencesManager = require(PreferencesManager.class);

		_serverName = UtilServer.getServerName();

		_redisManager = new PartyRedisManager(this, _serverName);

		_inviteManager = new PartyInviteManager(this);
		_joinManager = new PartyJoinManager(this);

		_region = UtilServer.getRegion();

		_messageManager = require(MessageManager.class);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.PARTY_COMMAND, true, true);
	}

	@Deprecated
	public void putIntoPendingJoin(UUID player, UUID party)
	{
		_pendingJoinMap.put(player, party);
	}

	@Override
	public void addCommands()
	{
		addCommand(new PartyCommand(this));
	}

	public Party getPartyByPlayer(UUID playerId)
	{
		return _partiesByPlayer.get(playerId);
	}

	public Party getPartyById(UUID playerId)
	{
		return _partiesById.get(playerId);
	}

	public Party getPartyByPlayer(Player player)
	{
		return _partiesByPlayer.get(player.getUniqueId());
	}

	public Collection<Party> getAllParties()
	{
		return _partiesById.values();
	}

	public void addParty(Party party)
	{
		_partiesById.put(party.getUniqueId(), party);
	}

	public Portal getPortal()
	{
		return _portal;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public PreferencesManager getPreferencesManager()
	{
		return _preferencesManager;
	}

	public PartyRedisManager getRedisManager()
	{
		return _redisManager;
	}

	public PartyInviteManager getInviteManager()
	{
		return _inviteManager;
	}

	public String getServerName()
	{
		return _serverName;
	}

	public Region getRegion()
	{
		return _region;
	}

	public void denyInviteBySender(Player caller, String senderName)
	{
		PartyInviteManager inviteManager = getInviteManager();

		if (!inviteManager.hasInviteFrom(caller, senderName))
		{
			UtilPlayer.message(caller, F.main("Party", "You do not have a pending invite to " + F.elem(senderName) + "'s party."));
			return;
		}

		InviteData invite = inviteManager.getInviteBySender(caller.getName(), senderName);

		inviteManager.removeInvite(caller, senderName);

		if (invite.getServerName().equals(_serverName))
		{
			Player senderPlayer = Bukkit.getPlayerExact(senderName);
			if (senderPlayer == null)
			{
				UtilPlayer.message(caller, F.main("Party", "You have denied your invite to " + F.elem(senderName) + "'s party, but it seems that " + F.elem(senderName) + " is no longer in this server"));
				return;
			}

			Party partyBySender = getPartyByPlayer(senderPlayer);

			if (partyBySender == null)
			{
				// todo send cancelled invitation msg when party is disbanded
				UtilPlayer.message(caller, F.main("Party", "You have denied your invite to " + F.elem(senderPlayer.getName()) + "'s party, but it seems that " + F.elem(senderPlayer.getName()) + " has disbanded his party as well"));
				return;
			}

			UtilPlayer.message(caller, F.main("Party", "You have denied your invite to " + F.elem(senderPlayer.getName()) + "'s party"));
			UtilPlayer.message(senderPlayer, F.main("Party", F.elem(caller.getName()) + " has denied your invite"));
		}
		else
		{
			Player senderPlayer = Bukkit.getPlayerExact(senderName);
			if (senderPlayer != null)
			{
				UtilPlayer.message(caller, F.main("Party", "You have denied your invite to " + F.elem(senderPlayer.getName()) + "'s party, but it seems that " + F.elem(senderPlayer.getName()) + " has joined you in this server"));
				return;
			}

			UtilPlayer.message(caller, F.main("Party", "You have denied your invite to " + F.elem(senderName) + "'s party"));

			ServerCommandManager.getInstance().publishCommand(new PartyCrossServerInviteDeny(
					caller.getName(),
					caller.getUniqueId(),
					invite.getInviterName(),
					invite.getInviterUUID(),
					invite.getPartyUUID()
			));
		}
	}

	public void acceptInviteBySender(Player caller, String senderName)
	{
		PartyInviteManager inviteManager = getInviteManager();

		if (!inviteManager.hasInviteFrom(caller, senderName))
		{
			UtilPlayer.message(caller, F.main("Party", "You do not have a pending invite to " + F.elem(senderName) + "'s party."));
			return;
		}

		if (getPartyByPlayer(caller) != null)
		{
			caller.sendMessage(F.main("Party", "Please leave your party before accepting another invite!"));
			return;
		}

		InviteData invite = inviteManager.getInviteBySender(caller.getName(), senderName);

		inviteManager.removeInvite(caller, senderName);

		if (invite.getServerName().equals(_serverName))
		{
			Player senderPlayer = Bukkit.getPlayerExact(senderName);
			if (senderPlayer == null)
			{
				UtilPlayer.message(caller, F.main("Party", "It seems that " + F.elem(senderName) + " is no longer in this server"));
				return;
			}

			Party partyBySender = getPartyByPlayer(senderPlayer);

			if (partyBySender == null)
			{
				// todo send cancelled invitation msg when party is disbanded
				UtilPlayer.message(caller, F.main("Party", "It seems that " + F.elem(senderName) + " has disbanded their party. Shucks!"));
				return;
			}
			addToParty(caller, partyBySender);
		}
		else
		{
			Player senderPlayer = Bukkit.getPlayerExact(senderName);
			if (senderPlayer != null)
			{
				// todo maybe auto create party (if there are no new desync issues)
				UtilPlayer.message(caller, F.main("Party", "Strange. It seems that " + F.elem(senderName) + " has preemptively joined you in this server. They'll need to resend an invitation"));
				return;
			}

			UtilPlayer.message(caller, F.main("Party", "Please wait while I attempt to locate " + F.elem(invite.getInviterName()) + "..."));

			ServerCommandManager.getInstance().publishCommand(new PartyCrossServerInviteAccept(
					caller.getName(),
					caller.getUniqueId(),
					invite.getInviterName(),
					invite.getInviterUUID(),
					invite.getPartyUUID()
			));

			runSyncLater(() ->
			{
				if (!caller.isOnline())
				{
					return;
				}

				UtilPlayer.message(caller, F.main("Party", "Uh oh. It looks like " + F.elem(invite.getInviterName()) + " has left the network - I couldn't find them!"));
			}, 20 * 5L);
		}
	}

	/**
	 * @param caller The player who initiated the request
	 * @param target The player's target
	 */
	public void invite(Player caller, String target)
	{
		if (target.equalsIgnoreCase(caller.getName()))
		{
			caller.sendMessage(F.main("Party", "You cannot invite yourself!"));
			return;
		}

		Player possible = Bukkit.getPlayerExact(target);

		Party party = getPartyByPlayer(caller);

		// preemptively create party - it might be a slight inconvenience but it saves a lot of untangling work
		if (party == null)
		{
			UtilPlayer.message(caller, F.main("Party", "You don't seem to have a party, so I've created a new one for you!"));
			party = new Party(caller);
			_partiesById.put(party.getUniqueId(), party);
			addToParty(caller, party);
		}

		if (!party.getOwnerName().equalsIgnoreCase(caller.getName()))
		{
			if (!_messageManager.isMuted(caller))
			{
				party.sendMessage(F.main("Party", F.elem(caller.getName()) + " has suggested " + F.elem(target) + " be invited"));
				party.getOwnerAsPlayer().ifPresent(owner ->
				{
					ChildJsonMessage message = new ChildJsonMessage("").extra(F.main("Party", "Click "));
					message.add(F.link("Invite " + target))
							.hover(HoverEvent.SHOW_TEXT, C.cGreen + "Clicking this will invite " + C.cYellow + target + C.cGreen + " to the party")
							.click(ClickEvent.RUN_COMMAND, "/party gui invite " + target);
					message.add(C.mBody + " to invite them");
					message.sendToPlayer(owner);
				});
			}
			return;
		}
		if (party.getMembers().size() >= party.getSize())
		{
			Lang.PARTY_FULL.send(caller);
			return;
		}
		if (possible != null && party.isMember(possible))
		{
			Lang.ALREADY_MEMBER.send(caller, target);
			return;
		}
		if (getInviteManager().getInviteBySender(target, caller.getName()) != null)
		{
			Lang.ALREADY_INVITED.send(caller, target);
			return;
		}
		if (possible != null && !getPreferencesManager().get(possible).isActive(Preference.PARTY_REQUESTS))
		{
			UtilPlayer.message(caller, F.main("Party", F.name(target) + " is not accepting invites at this time."));
			return;
		}

		//Same Server
		if (possible != null)
		{
			Lang.SUCCESS_INVITE.send(party, caller.getName(), target);
			getInviteManager().inviteTo(possible.getName(), possible.getUniqueId(), caller.getName(), caller.getUniqueId(), party.getUniqueId(), getServerName());
		}
		else
		{
			findAndInvite(caller, target, party);
		}
	}

	/**
	 * Initiates inviting a player who is no on the same server
	 */
	private void findAndInvite(Player caller, String target, Party destParty)
	{
		Map<String, BukkitTask> pendingInvites = getRedisManager().getPendingFindResponse().computeIfAbsent(caller.getUniqueId(), key -> new HashMap<>());
		if (!pendingInvites.containsKey(target))
		{
			caller.sendMessage(F.main("Party", "Attempting to invite " + F.elem(target) + "..."));
			pendingInvites.put(target, runSyncLater(new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!pendingInvites.containsKey(target))
					{
						return;
					}
					pendingInvites.remove(target);
					if (!caller.isOnline())
					{
						return;
					}

					Player targetPlayer = Bukkit.getPlayerExact(target);
					if (targetPlayer != null)
					{
						UtilPlayer.message(caller, F.main("Party", "Huh. We couldn't find " + F.elem(target) + ", but it seems they've joined you in your server"));
						return;
					}

					caller.sendMessage(F.main("Party", "Could not locate " + F.elem(target) + "."));
				}
			}, 20L * 5));

			ServerCommandManager.getInstance().publishCommand(new PartyCrossServerInviteCommand(caller, target, destParty));
		}
		else
		{
			caller.sendMessage(F.main("Party", "Please wait until the previous invite has completed"));
		}
	}

	public Party getPendingParty(Player player)
	{
		UUID partyUUID = _pendingJoinMap.get(player.getUniqueId());
		if (partyUUID == null) return null;
		for (Party party : _partiesById.values())
		{
			if (party.getUniqueId().equals(partyUUID))
				return party;
		}
		return null;
	}

	public boolean hasPendingJoin(Player player)
	{
		return _pendingJoinMap.containsKey(player.getUniqueId());
	}

	public void removePendingJoin(Player player)
	{
		_pendingJoinMap.remove(player.getUniqueId());
	}

	public void removePendingJoin(UUID player)
	{
		_pendingJoinMap.remove(player);
	}

	/**
	 * Kicks a player from the callers party
	 *
	 * @param caller The player who initiated the request
	 * @param target The player's target
	 */
	public void kickPlayer(Player caller, String target)
	{
		Party party = getPartyByPlayer(caller);

		if (party == null)
		{
			Lang.NO_PARTY.send(caller);
			return;
		}

		if (!party.isOwner(caller))
		{
			Lang.NOT_OWNER.send(caller);
			return;
		}

		Player playerTarget = Bukkit.getPlayerExact(target);

		if (playerTarget == null)
		{
			Lang.NOT_MEMBER.send(caller, target);
			return;
		}

		if (!party.isMember(playerTarget))
		{
			Lang.NOT_MEMBER.send(caller, target);
			return;
		}

		if (playerTarget == caller)
		{
			UtilPlayer.message(caller, F.main("Party", "You can't kick yourself!"));
			return;
		}

		removeFromParty(playerTarget, PartyRemoveReason.KICKED);
	}

	public void addToParty(Player player, Party party)
	{
		_partiesByPlayer.put(player.getUniqueId(), party);
		party.addMember(player);
	}

	/**
	 * Leaves the players current party if he is in one
	 *
	 * @param caller The player who wishes to leave his party
	 */
	public void leaveParty(Player caller)
	{
		Party party = getPartyByPlayer(caller);

		if (party == null)
		{
			Lang.NO_PARTY.send(caller);
			return;
		}

		Lang.LEFT.send(caller);
		removeFromParty(caller, PartyRemoveReason.LEFT);
	}

	public void removeFromParty(Player player, PartyRemoveReason reason)
	{
		Party party = _partiesByPlayer.remove(player.getUniqueId());

		if (party == null)
		{
			return;
		}

		if (player.getOpenInventory() != null)
		{
			if (Menu.get(player.getUniqueId()) != null)
			{
				player.closeInventory();
				Menu.remove(player.getUniqueId());
			}
		}

		party.removeMember(player);

		switch (reason)
		{
			case KICKED:
				Lang.REMOVE_PLAYER_KICK.send(party, player.getName());
				break;
			case LEFT:
				Lang.REMOVE_PLAYER.send(party, player.getName());
				break;
			case OTHER:
				break;
			case DISBANDED:
				break;
			case DISBANDED_BY_OWNER:
				break;
		}

		if (party.getMembers().size() == 0)
		{
			removeParty(party);
		}
	}

	/**
	 * Disbands a player's current party
	 *
	 * @param caller The player who wishes to disband his party
	 */
	public void disband(Player caller)
	{
		Party party = getPartyByPlayer(caller);

		if (party == null)
		{
			Lang.NO_PARTY.send(caller);
			return;
		}

		if (!party.isOwner(caller))
		{
			Lang.NOT_OWNER.send(caller);
			return;
		}

		caller.sendMessage(F.main("Party", "You have disbanded your party."));
		for (Player player : party.getMembers())
		{
			if (player != caller)
			{
				UtilPlayer.message(player, F.main("Party", "Your party has been disbanded!"));
			}
		}
		removeParty(party);
	}

	public void removeParty(Party party)
	{
		_joinManager.removePendingJoin(party);
		_partiesById.remove(party.getUniqueId());
		_partiesByPlayer.entrySet().removeIf(ent -> ent.getValue().equals(party));
		party.clear();
	}

	public void giveItemIfNotExists(Player player)
	{
		if (!UtilItem.isSimilar(player.getInventory().getItem(INTERFACE_SLOT), INTERFACE_ITEM, UtilItem.ItemAttribute.NAME, UtilItem.ItemAttribute.MATERIAL))
		{
			player.getInventory().setItem(PartyManager.INTERFACE_SLOT, PartyManager.INTERFACE_ITEM);
		}
	}
}
