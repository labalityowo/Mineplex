package mineplex.core.teleport.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class RedisLocate extends ServerCommand
{
	private String _sender;
	private UUID _senderUUID;
	private String _sendingServer;

	private String _target;

	private UUID _uuid = UUID.randomUUID();

	public RedisLocate(String sendingServer, String sender, UUID senderUUID, String target)
	{
		_sender = sender;
		_senderUUID = senderUUID;
		_target = target;
		_sendingServer = sendingServer;
	}

	public String getSender()
	{
		return _sender;
	}

	public String getServer()
	{
		return _sendingServer;
	}

	public String getTarget()
	{
		return _target;
	}

	public UUID getUUID()
	{
		return _uuid;
	}

	public UUID getSenderId()
	{
		return _senderUUID;
	}
}