package com.mineplex.clansqueue.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import mineplex.serverdata.Utility;
import mineplex.serverdata.servers.ServerManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class ClansQueueMessenger
{
	private static final String CHANNEL_NAME_BASE = "ClansQueueMessageChannel:";
	
	private static final Map<String, ClansQueueMessenger> _messengers = new ConcurrentHashMap<>();
	
	private final String _identifier;
	private final JedisPool _readPool;
	private final JedisPool _writePool;
	@SuppressWarnings("rawtypes")
	private final Map<String, Class> _bodyTypes = Collections.synchronizedMap(new HashMap<>());
	@SuppressWarnings("rawtypes")
	private final Map<String, List<BiConsumer>> _listeners = Collections.synchronizedMap(new HashMap<>());
	
	private ClansQueueMessenger(String identifier)
	{
		_identifier = identifier;
		
		_writePool = Utility.generatePool(ServerManager.getMasterConnection());
		_readPool = Utility.generatePool(ServerManager.getSlaveConnection());
		
		initialize();
	}
	
	private void initialize()
	{
		new Thread("Clans Queue Messenger: " + _identifier)
		{
			public void run()
			{
				try (Jedis jedis = _readPool.getResource())
				{
					jedis.subscribe(new ClansQueueMessageListener(ClansQueueMessenger.this), CHANNEL_NAME_BASE + "ALL", CHANNEL_NAME_BASE + _identifier);
				}
			}
		}.start();
	}
	
	public <T extends ClansQueueMessageBody> void registerListener(Class<T> messageType, BiConsumer<T, String> callback)
	{
		_bodyTypes.putIfAbsent(messageType.getName(), messageType);
		_listeners.computeIfAbsent(messageType.getName(), (type) -> new ArrayList<>()).add(callback);
	}
	
	public void transmitMessage(ClansQueueMessageBody message)
	{
		transmitMessage(message, "ALL");
	}
	
	public void transmitMessage(ClansQueueMessageBody message, String target)
	{
		ClansQueueMessage msg = new ClansQueueMessage();
		msg.Origin = _identifier;
		msg.BodyClass = message.getClass().getName();
		msg.BodySerialized = message.toString();
		
		final String toSend = Utility.serialize(msg);
		
		new Thread(() ->
		{
			try (Jedis jedis = _writePool.getResource())
			{
				jedis.publish(CHANNEL_NAME_BASE + target, toSend);
			}
		}).start();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ClansQueueMessageBody> void receiveMessage(ClansQueueMessage message)
	{
		if (_listeners.containsKey(message.BodyClass) && _bodyTypes.containsKey(message.BodyClass))
		{
			T body = Utility.deserialize(message.BodySerialized, (Class<T>)_bodyTypes.get(message.BodyClass));
			_listeners.get(message.BodyClass).forEach(listener -> listener.accept(body, message.Origin));
		}
	}
	
	private static class ClansQueueMessageListener extends JedisPubSub
	{
		private final ClansQueueMessenger _manager;
		
		private ClansQueueMessageListener(ClansQueueMessenger manager)
		{
			_manager = manager;
		}
		
		@Override
		public void onMessage(String channelName, String message)
		{
			ClansQueueMessage msg = Utility.deserialize(message, ClansQueueMessage.class);
			_manager.receiveMessage(msg);
		}
	}
	
	public static ClansQueueMessenger getMessenger(String identifier)
	{
		return _messengers.computeIfAbsent(identifier, (id) -> new ClansQueueMessenger(id));
	}
}