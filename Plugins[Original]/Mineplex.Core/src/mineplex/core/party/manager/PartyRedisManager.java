package mineplex.core.party.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.rediscommands.PartyCrossServerInviteAccept;
import mineplex.core.party.rediscommands.PartyCrossServerInviteCommand;
import mineplex.core.party.rediscommands.PartyCrossServerInviteDeny;
import mineplex.core.party.rediscommands.PartyCrossServerInviteResponse;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.serverdata.commands.ServerCommandManager;

public class PartyRedisManager
{
	private final Map<UUID, Map<String, BukkitTask>> _pendingFindResponse = new HashMap<>();

	private final PartyManager _plugin;
	private final String _serverName;

	public PartyRedisManager(PartyManager plugin, String serverName)
	{
		_plugin = plugin;
		_serverName = serverName;

		ServerCommandManager.getInstance().registerCommandType(PartyCrossServerInviteCommand.class, command ->
		{
			_plugin.runSync(() ->
			{
				Player player = Bukkit.getPlayerExact(command.getTarget());

				if (player == null || !player.isOnline())
				{
					return;
				}

				if (!_plugin.getPreferencesManager().get(player).isActive(Preference.PARTY_REQUESTS))
				{
					ServerCommandManager.getInstance().publishCommand(new PartyCrossServerInviteResponse(PartyCrossServerInviteResponse.Result.TARGET_NOT_ACCEPTING_INVITES, command, player));
					return;
				}

				_plugin.getInviteManager().inviteTo(player.getName(), player.getUniqueId(), command.getRequesterName(), command.getRequesterUUID(), command.getPartyUUID(), command.getFromServer());

				if (_plugin.getPartyByPlayer(player) != null)
				{
					ServerCommandManager.getInstance().publishCommand(new PartyCrossServerInviteResponse(PartyCrossServerInviteResponse.Result.SUCCESS_IN_PARTY, command, player));
				}
				else
				{
					ServerCommandManager.getInstance().publishCommand(new PartyCrossServerInviteResponse(PartyCrossServerInviteResponse.Result.SUCCESS, command, player));
				}
			});
		});

		ServerCommandManager.getInstance().registerCommandType(PartyCrossServerInviteResponse.class, command ->
		{
			if (!command.getOrigin().wasSentFromThisServer())
				return;

			_plugin.runSync(() ->
			{
				Map<String, BukkitTask> pendingTasks = _pendingFindResponse.get(command.getOrigin().getRequesterUUID());

				if (pendingTasks == null)
					return;

				BukkitTask alertTask = pendingTasks.remove(command.getOrigin().getTarget());

				if (alertTask == null)
					return;

				alertTask.cancel();

				Player caller = Bukkit.getPlayer(command.getOrigin().getRequesterUUID());

				if (caller == null || !caller.isOnline())
					return;

				switch (command.getResult())
				{
					case TARGET_NOT_ACCEPTING_INVITES:
						UtilPlayer.message(caller, F.main("Party", "The player " + F.elem(command.getOrigin().getTarget()) + " is not accepting invites!"));
						break;
					case SUCCESS_IN_PARTY:
					{
						Party party = _plugin.getPartyByPlayer(command.getOrigin().getRequesterUUID());
						if (party == null)
						{
							// todo wat do
							return;
						}
						if (!party.getOwnerName().equals(command.getOrigin().getRequesterName()))
						{
							//todo wat do
						}

						_plugin.getInviteManager().inviteTo(command.getTargetName(), command.getTargetUUID(), command.getOrigin().getRequesterName(), command.getOrigin().getRequesterUUID(), command.getOrigin().getPartyUUID(), _plugin.getServerName());
						party.sendMessage(F.main("Party", F.elem(command.getOrigin().getRequesterName()) + " has invited " + F.elem(command.getTargetName()) + " to the party"));
						break;
					}
					case SUCCESS:
					{
						Party party = _plugin.getPartyByPlayer(command.getOrigin().getRequesterUUID());
						if (party == null)
						{
							// todo wat do
							return;
						}
						if (!party.getOwnerName().equals(command.getOrigin().getRequesterName()))
						{
							//todo wat do
						}

						_plugin.getInviteManager().inviteTo(command.getTargetName(), command.getTargetUUID(), command.getOrigin().getRequesterName(), command.getOrigin().getRequesterUUID(), command.getOrigin().getPartyUUID(), _plugin.getServerName());
						party.sendMessage(F.main("Party", F.elem(command.getOrigin().getRequesterName()) + " has invited " + F.elem(command.getTargetName()) + " to the party"));
						break;
					}
					case UNKNOWN:
						UtilPlayer.message(caller, F.main("Party", "Uh oh, something went wrong while inviting " + F.elem(command.getTargetName())));
						break;
				}
			});
		});

		ServerCommandManager.getInstance().registerCommandType(PartyCrossServerInviteAccept.class, command ->
		{
			_plugin.runSync(() ->
			{
				Player apparentSender = Bukkit.getPlayer(command.getInviterUUID());
				if (apparentSender != null && apparentSender.isOnline())
				{
					_plugin.getInviteManager().removeInviteTo(command.getPlayerName(), command.getPartyUUID());

					Party partyOfSender = _plugin.getPartyByPlayer(apparentSender);
					if (partyOfSender == null)
					{
						//todo wat do
						return;
					}
					if (!partyOfSender.getUniqueId().equals(command.getPartyUUID()))
					{
						//todo wat do
						return;
					}

					if (!partyOfSender.getOwnerName().equals(command.getInviterName()))
					{
						// todo ignore for now but wat do
					}

					// we good
					_plugin.putIntoPendingJoin(command.getPlayerUUID(), partyOfSender.getUniqueId());
					Portal.transferPlayer(command.getPlayerName(), _serverName);
				}
			});
		});

		ServerCommandManager.getInstance().registerCommandType(PartyCrossServerInviteDeny.class, command ->
		{
			_plugin.runSync(() ->
			{
				Player apparentSender = Bukkit.getPlayer(command.getInviterUUID());
				if (apparentSender != null && apparentSender.isOnline())
				{
					_plugin.getInviteManager().removeInviteTo(command.getPlayerName(), command.getPartyUUID());

					Party partyOfSender = _plugin.getPartyByPlayer(apparentSender);
					if (partyOfSender == null)
					{
						UtilPlayer.message(apparentSender, F.main("Party", F.elem(command.getPlayerName()) + " has denied your invite, but it seems you don't have a party anymore"));
						return;
					}
					if (!partyOfSender.getUniqueId().equals(command.getPartyUUID()))
					{
						UtilPlayer.message(apparentSender, F.main("Party", F.elem(command.getPlayerName()) + " has denied your invite, but it seems that you've made a new party in the meantime"));
						return;
					}

					if (!partyOfSender.getOwnerName().equals(command.getInviterName()))
					{
						UtilPlayer.message(apparentSender, F.main("Party", F.elem(command.getPlayerName()) + " has denied your invite, but it seems that you are not the owner of the party anymore"));
						return;
					}

					UtilPlayer.message(apparentSender, F.main("Party", F.elem(command.getPlayerName()) + " has denied your invite"));
				}
			});
		});
	}

	public Map<UUID, Map<String, BukkitTask>> getPendingFindResponse()
	{
		return _pendingFindResponse;
	}
}