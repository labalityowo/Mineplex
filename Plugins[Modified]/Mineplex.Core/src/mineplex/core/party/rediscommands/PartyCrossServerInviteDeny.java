package mineplex.core.party.rediscommands;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class PartyCrossServerInviteDeny extends ServerCommand
{
	private final String _playerName;
	private final UUID _playerUUID;

	private final String _inviterName;
	private final UUID _inviterUUID;

	private final UUID _partyUUID;

	public PartyCrossServerInviteDeny(String playerName, UUID playerUUID, String inviterName, UUID inviterUUID, UUID partyUUID)
	{
		_playerName = playerName;
		_playerUUID = playerUUID;
		_inviterName = inviterName;
		_inviterUUID = inviterUUID;
		_partyUUID = partyUUID;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public UUID getPlayerUUID()
	{
		return _playerUUID;
	}

	public String getInviterName()
	{
		return _inviterName;
	}

	public UUID getInviterUUID()
	{
		return _inviterUUID;
	}

	public UUID getPartyUUID()
	{
		return _partyUUID;
	}
}
