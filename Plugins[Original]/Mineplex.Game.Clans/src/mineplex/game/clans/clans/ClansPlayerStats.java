package mineplex.game.clans.clans;

public enum ClansPlayerStats
{
	PLAY_TIME("Clans.TimePlaying");
	
	private String _id;
	
	ClansPlayerStats(String id)
	{
		_id = id;
	}
	
	public String id()
	{
		return _id;
	}
}
