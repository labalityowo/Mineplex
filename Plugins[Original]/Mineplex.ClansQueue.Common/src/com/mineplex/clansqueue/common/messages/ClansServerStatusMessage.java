package com.mineplex.clansqueue.common.messages;

import com.mineplex.clansqueue.common.ClansQueueMessageBody;

public class ClansServerStatusMessage extends ClansQueueMessageBody
{
	public String ServerName;
	public int OpenSlots;
	public boolean Online;
}