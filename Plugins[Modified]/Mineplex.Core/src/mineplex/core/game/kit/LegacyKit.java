package mineplex.core.game.kit;

@Deprecated
public class LegacyKit
{

	private final String _id;
	private final int _kitXP, _kitLevel, _kitUpgradeLevel;

	LegacyKit(String id, int kitXP, int kitLevel, int kitUpgradeLevel)
	{
		_id = id;
		_kitXP = kitXP;
		_kitLevel = kitLevel;
		_kitUpgradeLevel = kitUpgradeLevel;
	}

	public String getId()
	{
		return _id;
	}

	public int getKitXP()
	{
		return _kitXP;
	}

	public int getKitLevel()
	{
		return _kitLevel;
	}

	public int getKitUpgradeLevel()
	{
		return _kitUpgradeLevel;
	}
}
