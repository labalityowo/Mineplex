package mineplex.core.teleport.redis;

import java.util.Set;
import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class RankLocateCallback extends ServerCommand
{
	private String _serverName;
	private Set<String> _online;
	private UUID _receivingPlayerUUID;

	private UUID _uuid;

	public RankLocateCallback(RankLocate command, String serverName, Set<String> online)
	{
		_uuid = command.getUUID();
		_receivingPlayerUUID = command.getSenderUUID();
		_online = online;
		_serverName = serverName;

		setTargetServers(command.getSendingServer());
	}

	public Set<String> getOnline()
	{
		return _online;
	}

	public String getServerName()
	{
		return _serverName;
	}

	public UUID getReceivingPlayerUUID()
	{
		return _receivingPlayerUUID;
	}

	public UUID getUUID()
	{
		return _uuid;
	}
}