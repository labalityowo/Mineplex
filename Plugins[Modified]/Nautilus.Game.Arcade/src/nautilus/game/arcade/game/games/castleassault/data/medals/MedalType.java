package nautilus.game.arcade.game.games.castleassault.data.medals;

public enum MedalType
{
	ELIM("Eliminations"),
	KING_DMG("Damage on King"),
	OBJECTIVE_KILL("Objective Kills")
	;
	
	private String _name;
	
	private MedalType(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
}