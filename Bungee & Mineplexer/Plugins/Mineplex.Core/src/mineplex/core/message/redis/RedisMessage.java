package mineplex.core.message.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

/**
 * Used to send a admin or normal message between servers
 */
public class RedisMessage extends ServerCommand
{
    private String _message;
    private String _sender;
    private String _sendingServer;
    private String _target;
    private String _rank;
    private UUID _uuid = UUID.randomUUID();

    public RedisMessage(String sendingServer, String sender, String targetServer, String target, String message, String rank)
    {
        _sender = sender;
        _target = target;
        _message = message;
        _sendingServer = sendingServer;
        _rank = rank;

        if (targetServer != null)
        {
            setTargetServers(targetServer);
        }
    }

    public UUID getUUID()
    {
        return _uuid;
    }

    public String getRank()
    {
        return _rank;
    }

    public boolean isStaffMessage()
    {
        return getTargetServers().length == 0;
    }

    public String getMessage()
    {
        return _message;
    }

    public String getSender()
    {
        return _sender;
    }

    public String getSendingServer()
    {
        return _sendingServer;
    }

    public String getTarget()
    {
        return _target;
    }

    @Override
    public void run()
    {
        // Utilitizes a callback functionality to seperate dependencies
    }
}