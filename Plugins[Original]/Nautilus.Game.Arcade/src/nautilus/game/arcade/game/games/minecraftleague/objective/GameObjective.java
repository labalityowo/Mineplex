package nautilus.game.arcade.game.games.minecraftleague.objective;


public abstract class GameObjective
{
	private String _displayText, _id;
	
	public GameObjective(String id, String displayText)
	{
		_id = id;
		_displayText = displayText;
	}
	
	public String getID()
	{
		return _id;
	}
	
	public String getDisplayText()
	{
		return _displayText;
	}
}
