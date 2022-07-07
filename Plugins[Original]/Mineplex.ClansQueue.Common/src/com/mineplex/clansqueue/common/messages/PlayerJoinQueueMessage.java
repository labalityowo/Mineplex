package com.mineplex.clansqueue.common.messages;

import java.util.UUID;

import com.mineplex.clansqueue.common.ClansQueueMessageBody;

public class PlayerJoinQueueMessage extends ClansQueueMessageBody
{
	public UUID PlayerUUID;
	public String TargetServer;
	public int PlayerPriority;
}