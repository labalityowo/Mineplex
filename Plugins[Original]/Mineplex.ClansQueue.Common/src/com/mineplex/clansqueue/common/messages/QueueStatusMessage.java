package com.mineplex.clansqueue.common.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mineplex.clansqueue.common.ClansQueueMessageBody;

public class QueueStatusMessage extends ClansQueueMessageBody
{
	public final List<QueueSnapshot> Snapshots = new ArrayList<>();
	
	public static class QueueSnapshot
	{
		public String ServerName;
		public Map<UUID, Integer> Queue;
		public boolean Paused;
	}
}