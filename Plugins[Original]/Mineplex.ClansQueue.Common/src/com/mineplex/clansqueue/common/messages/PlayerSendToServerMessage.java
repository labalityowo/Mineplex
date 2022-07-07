package com.mineplex.clansqueue.common.messages;

import java.util.UUID;

import com.mineplex.clansqueue.common.ClansQueueMessageBody;

public class PlayerSendToServerMessage extends ClansQueueMessageBody
{
	public UUID PlayerUUID;
	public String TargetServer;
}