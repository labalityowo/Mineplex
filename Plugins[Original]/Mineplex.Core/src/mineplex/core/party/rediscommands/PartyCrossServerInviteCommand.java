package mineplex.core.party.rediscommands;

import java.util.UUID;

import org.bukkit.entity.Player;

import mineplex.core.party.Party;
import mineplex.serverdata.commands.ServerCommand;

public class PartyCrossServerInviteCommand extends ServerCommand
{
	private final String _requesterName;
	private final UUID _requesterUUID;
	private final String _target;
	private final UUID _partyUUID;

	public PartyCrossServerInviteCommand(Player caller, String target, Party destParty)
	{
		_target = target;
		_requesterName = caller.getName();
		_requesterUUID = caller.getUniqueId();
		_partyUUID = destParty.getUniqueId();
	}

	public String getTarget()
	{
		return _target;
	}

	public String getRequesterName()
	{
		return _requesterName;
	}

	public UUID getRequesterUUID()
	{
		return _requesterUUID;
	}

	public UUID getPartyUUID()
	{
		return _partyUUID;
	}
}
