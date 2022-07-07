package com.mineplex.clansqueue.service.queue;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ClansServer
{
	private final String _serverName;
	
	@GuardedBy("this")
	private boolean _online = false;
	
	@GuardedBy("this")
	private int _openSlots = 0;
	
	public ClansServer(String serverName)
	{
		_serverName = serverName;
	}
	
	public String getName()
	{
		return _serverName;
	}
	
	public synchronized boolean isOnline()
	{
		return _online;
	}
	
	public synchronized void setOnline(boolean online)
	{
		_online = online;
	}
	
	public synchronized int getOpenSlots()
	{
		return _openSlots;
	}
	
	public synchronized void setOpenSlots(int openSlots)
	{
		_openSlots = openSlots;
	}
	
	@Override
	public int hashCode()
	{
		return _serverName.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !getClass().isInstance(o))
		{
			return false;
		}
		
		return ((ClansServer)o)._serverName.equals(_serverName);
	}
}