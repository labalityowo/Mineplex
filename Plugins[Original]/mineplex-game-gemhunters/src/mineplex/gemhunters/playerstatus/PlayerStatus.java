package mineplex.gemhunters.playerstatus;

import mineplex.core.common.util.UtilTime;

public class PlayerStatus
{

	private PlayerStatusType _statusType;
	private long _start;
	private long _length;
	
	public PlayerStatus(PlayerStatusType statusType)
	{
		this(statusType, -1);
	}
	
	public PlayerStatus(PlayerStatusType statusType, long length)
	{
		_statusType = statusType;
		_start = System.currentTimeMillis();
		_length = length;
	}
	
	public PlayerStatusType getStatusType()
	{
		return _statusType;
	}
	
	public long getStart()
	{
		return _start;
	}
	
	public long getLength()
	{
		return _length;
	}
	
	public boolean isDone()
	{
		return _length > 0 && UtilTime.elapsed(_start, _length);
	}
	
}
