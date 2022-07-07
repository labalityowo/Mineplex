package mineplex.core.teleport.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class RankLocate extends ServerCommand
{
	private final String _sendingServer;
	private final String _senderName;
	private final UUID _senderUUID;
	private final UUID _uuid;
	private final String _rankIdentifier;
	
	public RankLocate(String sendingServer, String senderName, UUID senderUUID, String rankIdentifier)
	{
		_sendingServer = sendingServer;
		_senderName = senderName;
		_senderUUID = senderUUID;
		_uuid = UUID.randomUUID();
		_rankIdentifier = rankIdentifier;
	}
	
	public String getSendingServer()
	{
		return _sendingServer;
	}
	
	public String getSenderName()
	{
		return _senderName;
	}
	
	public UUID getSenderUUID()
	{
		return _senderUUID;
	}
	
	public UUID getUUID()
	{
		return _uuid;
	}
	
	public String getRankIdentifier()
	{
		return _rankIdentifier;
	}
}