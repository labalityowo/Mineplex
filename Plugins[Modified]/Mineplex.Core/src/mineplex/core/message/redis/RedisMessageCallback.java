package mineplex.core.message.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

/**
 * Used as a response in return to a admin or normal message between servers.
 */
public class RedisMessageCallback extends ServerCommand
{
	private String _message;
	private String _setLastMessage;
	private String _target;
	private boolean _staffMessage, _informMuted;
	private UUID _uuid;

	public RedisMessageCallback(RedisMessage globalMessage, boolean staffMessage, String receivedPlayer, String message, boolean informMuted)
	{
		_target = globalMessage.getSender();
		_message = message;
		_setLastMessage = receivedPlayer;
		_uuid = globalMessage.getUUID();
		_staffMessage = staffMessage;
		_informMuted = informMuted;

		if (globalMessage.getSendingServer() != null)
		{
			setTargetServers(globalMessage.getSendingServer());
		}
	}

	public boolean isStaffMessage()
	{
		return _staffMessage;
	}
	
	public boolean informMuted()
	{
		return _informMuted;
	}

	public String getLastReplied()
	{
		return _setLastMessage;
	}

	public String getMessage()
	{
		return _message;
	}

	public String getTarget()
	{
		return _target;
	}

	public UUID getUUID()
	{
		return _uuid;
	}

	@Override
	public void run()
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}