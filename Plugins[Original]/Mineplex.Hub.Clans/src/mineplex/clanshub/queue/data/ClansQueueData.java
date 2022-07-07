package mineplex.clanshub.queue.data;

public class ClansQueueData
{
	public final String ServerName;
	public int QueueMembers;
	public boolean QueuePaused;
	
	public ClansQueueData(String serverName)
	{
		ServerName = serverName;
	}
	
	@Override
	public int hashCode()
	{
		return ServerName.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !getClass().isInstance(o))
		{
			return false;
		}
		
		return ((ClansQueueData)o).ServerName.equals(ServerName);
	}
}