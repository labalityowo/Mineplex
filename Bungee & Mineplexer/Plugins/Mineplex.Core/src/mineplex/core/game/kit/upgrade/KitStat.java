package mineplex.core.game.kit.upgrade;

import java.util.Optional;

public enum KitStat
{

	XP(0, "XP"),
	UPGRADE_LEVEL(1, "Upgrade Level"),

	;

	public static Optional<KitStat> getById(int id)
	{
		for (KitStat stats : values())
		{
			if (stats._id == id)
			{
				return Optional.of(stats);
			}
		}

		return Optional.empty();
	}

	private final int _id;
	private final String _name;

	KitStat(int id, String name)
	{
		_id = id;
		_name = name;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}
}
