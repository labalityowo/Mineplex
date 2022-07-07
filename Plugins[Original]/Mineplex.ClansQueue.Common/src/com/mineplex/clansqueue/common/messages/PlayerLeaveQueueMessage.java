package com.mineplex.clansqueue.common.messages;

import java.util.UUID;

import com.mineplex.clansqueue.common.ClansQueueMessageBody;

public class PlayerLeaveQueueMessage extends ClansQueueMessageBody
{
	public UUID PlayerUUID;
	public String TargetServer;
}