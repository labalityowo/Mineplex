package mineplex.serverdata.commands;

import redis.clients.jedis.JedisPubSub;

/**
 * The ServerCommandListener listens for published Redis network messages
 * and deserializes them into their {@link ServerCommand} form, then registers
 * it's arrival at the {@link ServerCommandManager}.
 * @author Ty
 *
 */
public class ServerCommandListener extends JedisPubSub
{

	@Override
	public void onPMessage(String pattern, String channelName, String message) 
	{
		try
		{
			String commandType = channelName.split(":")[1];
			ServerCommandManager.getInstance().handleCommand(commandType, message);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	

	@Override
	public void onMessage(String channelName, String message)
	{
		
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels)
	{
		
	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels)
	{
		
	}

	@Override
	public void onSubscribe(String channelName, int subscribedChannels) 
	{
		
	}

	@Override
	public void onUnsubscribe(String channelName, int subscribedChannels) 
	{
		
	}

}
