package nautilus.game.arcade.game.games.minecraftleague;

public enum DataLoc
{
	//Base
	RED_CRYSTAL(" RED CRYSTAL"),
	RED_TOWER(" RED TOWER"),
	BLUE_CRYSTAL(" BLUE CRYSTAL"),
	BLUE_TOWER(" BLUE TOWER"),
	VARIANT_BASE("GAMEMODE "),
	//RED_BEACON("PINK"),
	/*BLUE_*/BEACON("CYAN"),
	SKELETON_SPAWNER("BROWN"),
	//MAP_DIAMOND("LIGHT_BLUE"),
	//MAP_IRON("SILVER"),
	RED_ORE("15"),
	BLUE_ORE("14"),
	DIAMOND_ORE("56"),
	COAL_ORE("16"),
	MOSH_IRON("129"),
	GRIND_AREA(" GRIND"),
	
	//Wither
	WITHER_WAYPOINT("PURPLE"),
	TOWER_WAYPOINT(" $team$ WITHER $number$"),
	WITHER_SKELETON("BLACK"),
	BLUE_ALTAR("LIME"),
	RED_ALTAR("YELLOW")
	;
	
	private String _key;
	
	private DataLoc(String key)
	{
		_key = key;
	}
	
	public String getKey()
	{
		return _key;
	}
}
