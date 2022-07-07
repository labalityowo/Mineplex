package com.mineplex.clansqueue.service;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mineplex.clansqueue.common.ClansQueueMessenger;
import com.mineplex.clansqueue.common.QueueConstant;
import com.mineplex.clansqueue.common.messages.ClansServerStatusMessage;
import com.mineplex.clansqueue.common.messages.PlayerJoinQueueMessage;
import com.mineplex.clansqueue.common.messages.PlayerLeaveQueueMessage;
import com.mineplex.clansqueue.common.messages.QueuePauseUpdateMessage;
import com.mineplex.clansqueue.common.messages.ServerOfflineMessage;
import com.mineplex.clansqueue.common.messages.ServerOnlineMessage;
import com.mineplex.clansqueue.service.commands.CommandSystem;
import com.mineplex.clansqueue.service.commands.ConsoleCommand;
import com.mineplex.clansqueue.service.queue.ClansQueueManager;

import mineplex.serverdata.Region;

public class QueueService
{
	public static void main(String[] args)
	{
		QueueService service = new QueueService(new File("eu.dat").exists());
		service.start();
		while (service.isRunning()) {}
		System.exit(0);
	}
	
	private final Region _region;
	private boolean _running = false;
	private final Map<String, ConsoleCommand> _commandMap = Collections.synchronizedMap(new HashMap<>());
	private final CommandSystem _commandSystem;
	private final ClansQueueManager _queueManager;
	
	private QueueService(boolean eu)
	{
		if (eu)
		{
			_region = Region.EU;
		}
		else
		{
			_region = Region.US;
		}
		_commandSystem = new CommandSystem(this, _commandMap);
		_queueManager = new ClansQueueManager(this);
	}
	
	private synchronized void start()
	{
		System.out.println("[Queue Service] Enabling on region " + getRegion().name());
		_running = true;
		_commandSystem.start();
		
		ClansQueueMessenger messenger = ClansQueueMessenger.getMessenger(QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
		messenger.registerListener(ServerOnlineMessage.class, (online, origin) -> _queueManager.handleServerEnable(online.ServerName));
		messenger.registerListener(ServerOfflineMessage.class, (offline, origin) -> _queueManager.handleServerDisable(offline.ServerName));
		messenger.registerListener(QueuePauseUpdateMessage.class, (pause, origin) -> _queueManager.handleQueuePause(pause.ServerName, pause.Paused));
		messenger.registerListener(PlayerJoinQueueMessage.class, (join, origin) -> _queueManager.joinQueue(join.TargetServer, origin, join.PlayerUUID, join.PlayerPriority));
		messenger.registerListener(PlayerLeaveQueueMessage.class, (leave, origin) -> _queueManager.leaveQueue(leave.TargetServer, leave.PlayerUUID));
		messenger.registerListener(ClansServerStatusMessage.class, (status, origin) -> _queueManager.handleServerUpdate(status.ServerName, status.OpenSlots, status.Online));
	}
	
	public ClansQueueManager getQueueManager()
	{
		return _queueManager;
	}
	
	public synchronized boolean isRunning()
	{
		return _running;
	}
	
	public Region getRegion()
	{
		return _region;
	}
	
	public void registerCommand(ConsoleCommand command)
	{
		_commandMap.put(command.getCommand().toLowerCase(), command);
	}
	
	public synchronized void shutdown()
	{
		System.out.println("[Queue Service] Shutting down...");
		_queueManager.stop();
		_running = false;
	}
}