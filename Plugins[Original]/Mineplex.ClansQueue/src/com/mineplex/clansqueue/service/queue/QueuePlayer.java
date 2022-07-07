package com.mineplex.clansqueue.service.queue;

import java.util.UUID;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Preconditions;
import com.mineplex.clansqueue.common.QueueConstant;

@NotThreadSafe
public class QueuePlayer implements Comparable<QueuePlayer>
{
	public final UUID PlayerUUID;
	public final String CurrentServer;
	public final long EntryTime;
	public int Weight;
	public long LastWeightIncrease;
	public int Position;
	
	public QueuePlayer(UUID uuid, String currentServer, int weight)
	{
		PlayerUUID = uuid;
		CurrentServer = currentServer;
		EntryTime = System.currentTimeMillis();
		Weight = weight;
		LastWeightIncrease = System.currentTimeMillis();
	}
	
	private void updateWeight()
	{
		if (Weight < QueueConstant.MAXIMUM_WEIGHT_FROM_INCREASE && (LastWeightIncrease + QueueConstant.TIME_TO_INCREASE_WEIGHT) < System.currentTimeMillis())
		{
			Weight++;
			LastWeightIncrease = System.currentTimeMillis();
		}
	}
	
	public ImmutableQueuePlayer immutable()
	{
		return new ImmutableQueuePlayer(PlayerUUID, CurrentServer, Position);
	}
	
	@Override
	public int hashCode()
	{
		return PlayerUUID.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !getClass().isInstance(o))
		{
			return false;
		}
		
		return ((QueuePlayer)o).PlayerUUID.equals(PlayerUUID);
	}

	@Override
	public int compareTo(QueuePlayer player)
	{
		Preconditions.checkNotNull(player);
		
		updateWeight();
		player.updateWeight();
		
		if (Weight == player.Weight)
		{
			return Long.compare(EntryTime, player.EntryTime);
		}
		else if (Weight > player.Weight)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}
	
	@Immutable
	public static class ImmutableQueuePlayer
	{
		public final UUID PlayerUUID;
		public final String CurrentServer;
		public final int Position;
		
		private ImmutableQueuePlayer(UUID uuid, String server, int position)
		{
			PlayerUUID = uuid;
			CurrentServer = server;
			Position = position;
		}
	}
}