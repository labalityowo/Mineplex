package mineplex.core.teleport.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class RedisLocateCallback extends ServerCommand
{
	private String _locatedPlayer;
	private String _server;
	private String _receivingPlayer;
	private UUID _receivingPlayerUUID;

	private UUID _uuid;

	public RedisLocateCallback(RedisLocate command, String server, String targetName)
	{
		_uuid = command.getUUID();
		_receivingPlayer = command.getSender();
		_receivingPlayerUUID = command.getSenderId();
		_locatedPlayer = targetName;
		_server = server;

		setTargetServers(command.getServer());
	}

	public String getLocatedPlayer()
	{
		return _locatedPlayer;
	}

	public String getServer()
	{
		return _server;
	}

	public String getReceivingPlayer()
	{
		return _receivingPlayer;
	}

	public UUID getReceivingPlayerId()
	{
		return _receivingPlayerUUID;
	}

	public UUID getUUID()
	{
		return _uuid;
	}
}