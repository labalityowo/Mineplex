package mineplex.serverdata.commands;

import java.util.UUID;

public class TransferUUIDCommand extends ServerCommand
{
	private final UUID _playerUUID;
	private final String _targetServer;

	public TransferUUIDCommand(UUID playerUUID, String targetServer)
	{
		_playerUUID = playerUUID;
		_targetServer = targetServer;
	}

	public UUID getPlayerUUID()
	{
		return _playerUUID;
	}

	public String getTargetServer()
	{
		return _targetServer;
	}
}
