package com.mineplex.clansqueue.common;

import mineplex.serverdata.Utility;

public abstract class ClansQueueMessageBody
{
	@Override
	public final String toString()
	{
		return Utility.serialize(this);
	}
}