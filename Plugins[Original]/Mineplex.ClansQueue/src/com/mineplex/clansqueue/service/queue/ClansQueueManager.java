package com.mineplex.clansqueue.service.queue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.mineplex.clansqueue.common.ClansQueueMessenger;
import com.mineplex.clansqueue.common.QueueConstant;
import com.mineplex.clansqueue.common.messages.PlayerJoinQueueCallbackMessage;
import com.mineplex.clansqueue.common.messages.PlayerSendToServerMessage;
import com.mineplex.clansqueue.common.messages.QueueDeleteMessage;
import com.mineplex.clansqueue.common.messages.QueuePauseBroadcastMessage;
import com.mineplex.clansqueue.common.messages.QueueStatusMessage;
import com.mineplex.clansqueue.common.messages.QueueStatusMessage.QueueSnapshot;
import com.mineplex.clansqueue.service.QueueService;

public class ClansQueueManager
{
	private final Map<String, ClansServer> _servers = new HashMap<>();
	private final Map<ClansServer, ServerQueue> _queues = new HashMap<>();
	private final ScheduledFuture<?> _updater;
	
	public ClansQueueManager(QueueService service)
	{
		_updater = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() ->
		{
			if (service.isRunning())
			{
				updateQueues();
			}
		}, 0, 5, TimeUnit.SECONDS);
	}
	
	private QueueStatusMessage buildStatusMessage(Collection<ServerQueue> queues)
	{
		QueueStatusMessage message = new QueueStatusMessage();
		
		queues.forEach(queue ->
		{
			QueueSnapshot snapshot = new QueueSnapshot();
			snapshot.Paused = queue.isPaused();
			snapshot.ServerName = queue.getServer().getName();
			snapshot.Queue = new HashMap<>();
			queue.getPlayers().values().forEach(player -> snapshot.Queue.put(player.PlayerUUID, player.Position));
			
			message.Snapshots.add(snapshot);
		});
		
		return message;
	}
	
	private synchronized void updateQueues()
	{
		System.out.println("Updating queues");
		Collection<ServerQueue> queues = _queues.values();
		
		queues.forEach(q ->
		{
			q.updatePositions(Math.min(q.getServer().getOpenSlots(), QueueConstant.MAX_TRANSFERS_PER_UPDATE));
			if (q.getServer().isOnline())
			{
				q.getNextSend().entrySet().forEach(entry ->
				{
					PlayerSendToServerMessage message = new PlayerSendToServerMessage();
					message.PlayerUUID = entry.getKey();
					message.TargetServer = q.getServer().getName();
					ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER).transmitMessage(message, entry.getValue());
				});
			}
		});
		
		QueueStatusMessage message = buildStatusMessage(queues);
		ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER).transmitMessage(message);
	}
	
	public synchronized ClansServer getLoadedServer(String serverName)
	{
		return _servers.get(serverName);
	}
	
	public synchronized Collection<ClansServer> getLoadedServers()
	{
		return Collections.unmodifiableCollection(_servers.values());
	}
	
	public synchronized void deleteServer(ClansServer server)
	{
		_servers.remove(server.getName());
		_queues.remove(server);
		QueueDeleteMessage message = new QueueDeleteMessage();
		ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER).transmitMessage(message);
	}
	
	public synchronized void handleServerEnable(String serverName)
	{
		_servers.computeIfAbsent(serverName, (name) ->
		{
			ClansServer server = new ClansServer(name);
			
			_queues.put(server, new ServerQueue(server));
			
			return server;
		}).setOnline(true);
		
		System.out.println("Clans server " + serverName + " enabled.");
	}
	
	public synchronized void handleServerDisable(String serverName)
	{
		_servers.computeIfAbsent(serverName, (name) ->
		{
			ClansServer server = new ClansServer(name);
			
			_queues.put(server, new ServerQueue(server));
			
			return server;
		}).setOnline(false);
	}
	
	public synchronized void handleServerUpdate(String serverName, int openSlots, boolean online)
	{
		ClansServer server = _servers.computeIfAbsent(serverName, (name) ->
		{
			ClansServer s = new ClansServer(name);
			
			_queues.put(s, new ServerQueue(s));
			
			return s;
		});
		server.setOpenSlots(openSlots);
		server.setOnline(online);
	}
	
	public synchronized void handleQueuePause(String serverName, boolean pause)
	{
		ClansServer server = _servers.get(serverName);
		if (server != null)
		{
			_queues.get(server).setPaused(pause);
			System.out.println("Clans server " + serverName + " queue pause: " + pause);
			QueuePauseBroadcastMessage message = new QueuePauseBroadcastMessage();
			message.ServerName = serverName;
			message.Paused = pause;
			ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER).transmitMessage(message);
		}
	}
	
	public synchronized void joinQueue(String serverName, String currentServer, UUID uuid, int weight)
	{
		ClansServer server = _servers.get(serverName);
		if (server != null)
		{
			ServerQueue queue = _queues.get(server);
			if (weight == QueueConstant.BYPASS_QUEUE_WEIGHT)
			{
				queue.addBypasser(uuid, currentServer);
			}
			else
			{
				queue.addPlayer(uuid, currentServer, weight, player ->
				{
					PlayerJoinQueueCallbackMessage message = new PlayerJoinQueueCallbackMessage();
					message.PlayerUUID = uuid;
					message.TargetServer = serverName;
					message.Position = player.Position;
					ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER).transmitMessage(message, currentServer);
					QueueStatusMessage update = buildStatusMessage(Arrays.asList(queue));
					ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER).transmitMessage(update);
				});
			}
		}
	}
	
	public synchronized void leaveQueue(String serverName, UUID uuid)
	{
		ClansServer server = _servers.get(serverName);
		if (server != null)
		{
			ServerQueue queue = _queues.get(server);
			queue.removePlayer(uuid, () ->
			{
				QueueStatusMessage message = buildStatusMessage(Arrays.asList(queue));
				ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER).transmitMessage(message);
			});
		}
	}
	
	public void stop()
	{
		_updater.cancel(true);
	}
}