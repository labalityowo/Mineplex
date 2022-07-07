package com.mineplex.clansqueue.service.queue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.mineplex.clansqueue.common.EnclosedInteger;
import com.mineplex.clansqueue.common.SortableLinkedList;
import com.mineplex.clansqueue.service.queue.QueuePlayer.ImmutableQueuePlayer;

@ThreadSafe
public class ServerQueue
{
	private final ClansServer _server;
	
	@GuardedBy("_sendLock")
	private final Map<UUID, String> _sending = new LinkedHashMap<>();
	
	@GuardedBy("_bypassLock")
	private final Map<UUID, String> _bypassing = new LinkedHashMap<>();
	
	@GuardedBy("_queueLock")
	private final SortableLinkedList<QueuePlayer> _queued = new SortableLinkedList<>();
	
	private final Object _bypassLock = new Object();
	private final Object _queueLock = new Object();
	private final Object _sendLock = new Object();
	private final Object _pauseLock = new Object();
	
	@GuardedBy("_pauseLock")
	private boolean _paused = false;
	
	public ServerQueue(ClansServer server)
	{
		_server = server;
	}
	
	private void sortQueue()
	{
		synchronized (_queueLock)
		{
			_queued.sort();
			EnclosedInteger position = new EnclosedInteger(1);
			_queued.forEach(qp ->
			{
				qp.Position = position.getAndIncrement();
			});
		}
	}
	
	public ClansServer getServer()
	{
		return _server;
	}
	
	public boolean isPaused()
	{
		if (!_server.isOnline())
		{
			return true;
		}
		
		synchronized (_pauseLock)
		{
			return _paused;
		}
	}
	
	public Map<UUID, String> getNextSend()
	{
		synchronized (_sendLock)
		{
			Map<UUID, String> sending = new LinkedHashMap<>();
			sending.putAll(_sending);
			_sending.clear();
			return sending;
		}
	}
	
	public Map<UUID, ImmutableQueuePlayer> getPlayers()
	{
		synchronized (_queueLock)
		{
			Map<UUID, ImmutableQueuePlayer> players = new LinkedHashMap<>();
			sortQueue();
			_queued.forEach(qp -> players.put(qp.PlayerUUID, qp.immutable()));
			
			return players;
		}
	}
	
	public void addBypasser(UUID uuid, String currentServer)
	{
		synchronized (_bypassLock)
		{
			_bypassing.put(uuid, currentServer);
		}
	}
	
	public void addPlayer(UUID uuid, String currentServer, int weight, Consumer<QueuePlayer> callback)
	{
		synchronized (_queueLock)
		{
			QueuePlayer player = new QueuePlayer(uuid, currentServer, weight);
			_queued.add(player);
			
			sortQueue();
			
			if (callback != null)
			{
				callback.accept(player);
			}
		}
	}
	
	public void removePlayer(UUID uuid, Runnable after)
	{
		synchronized (_queueLock)
		{
			_queued.removeIf(player -> player.PlayerUUID.equals(uuid));
			
			sortQueue();
			
			if (after != null)
			{
				after.run();
			}
		}
	}
	
	public void setPaused(boolean paused)
	{
		synchronized (_pauseLock)
		{
			_paused = paused;
		}
	}
	
	public void updatePositions(int openPlayerSlots)
	{
		Map<UUID, String> send = new LinkedHashMap<>();
		if (_server.isOnline())
		{
			synchronized (_bypassLock)
			{
				send.putAll(_bypassing);
				_bypassing.clear();
			}
		}
		synchronized (_queueLock)
		{
			if (!isPaused() && openPlayerSlots > 0)
			{
				sortQueue();
				while (send.size() < openPlayerSlots)
				{
					QueuePlayer player = _queued.poll();
					if (player == null)
					{
						break;
					}
					send.put(player.PlayerUUID, player.CurrentServer);
				}
				sortQueue();
			}
		}
		if (send.isEmpty())
		{
			return;
		}
		synchronized (_sendLock)
		{
			_sending.putAll(send);
		}
	}
}