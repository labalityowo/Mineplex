package mineplex.game.clans.clans;

public enum ClansPlayerTasks
{
	FIRST_SESSION("Clans.FirstSession");
	
	private String _id;
	
	ClansPlayerTasks(String id)
	{
		_id = id;
	}
	
	public String id()
	{
		return _id;
	}
}
