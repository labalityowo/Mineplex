package mineplex.core.gadget.types;

/**
 * The type of the different gadgets with some extra name data
 */
public enum GadgetType
{
	ITEM("Items", "activeItem", true),
	MORPH("Morphs", "activeMorph", true),
	PARTICLE("Particles", "activeParticle", false),
	MUSIC_DISC("Music Discs", "", true),
	COSTUME("Costumes", "", true),
	DOUBLE_JUMP("Leap Effects", "activeDoubleJump", false),
	ARROW_TRAIL("Arrow Trails", "activeArrowTrail", false),
	DEATH("Death Effects", "activeDeathEffect", false),
	HAT("Hats", "activeHat", true),
	TAUNT("Taunts", "activeTaunt", false),
	WIN_EFFECT("Win Effects", "activeWinEffect", false),
	GAME_MODIFIER("Game Cosmetics", "", false),
	BALLOON("Balloons", "", true),
	KIT_SELECTOR("Kit Selectors", "activeKitSelector", false),
	FLAG("Flags", "activeFlag", true),
	MOUNT("Mounts", "", true),
	WEAPON_NAME("Weapon Names", "activeWeapon", false),
	LEVEL_PREFIX("Level Prefixes", "activeLevelPrefix", false)

	;
	
	private final String _name, _databaseKey;
	private final boolean _disableForGame;
	
	GadgetType(String name, String databaseKey, boolean disableForGame)
	{
		_name = name;
		_databaseKey = databaseKey;
		_disableForGame = disableForGame;
	}
	
	/**
	 * Returns the category name of the gadget type in plural form
	 */
	public String getCategoryType()
	{
		return _name;
	}

	/**
	 * Returns the name of the database key used to store the active gadgets
	 */
	public String getDatabaseKey()
	{
		return _databaseKey;
	}

	public boolean disableForGame()
	{
		return _disableForGame;
	}

	/**
	 * @return The name of this category type, without the s at the end,
	 * if it has an s at the end. e.g. "Hats" will return "Hat" instead.
	 */
	public String getSingularType()
	{
		if (!_name.toLowerCase().endsWith("s"))
		{
			return _name;
		}

		return _name.substring(0, _name.length() - 1);
	}
}
