package mineplex.clanshub.queue.data;

import java.util.UUID;

public class QueuePlayerData
{
	public final UUID UniqueId;
	
	public boolean Queued;
	
	public int QueuePosition;
	
	public String TargetServer;
	
	public QueuePlayerData(UUID uuid)
	{
		UniqueId = uuid;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !getClass().isInstance(o))
		{
			return false;
		}
		
		return UniqueId.equals(((QueuePlayerData)o).UniqueId);
	}
	
	@Override
	public int hashCode()
	{
		return UniqueId.hashCode();
	}
}