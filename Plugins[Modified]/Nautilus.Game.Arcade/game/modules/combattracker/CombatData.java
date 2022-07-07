package nautilus.game.arcade.game.modules.combattracker;

public class CombatData
{

	private int _kills, _assits;

	public void incrementKills()
	{
		_kills++;
	}

	public int getKills()
	{
		return _kills;
	}

	public void incrementAssists()
	{
		_assits++;
	}


	public int getAssits()
	{
		return _assits;
	}
}
