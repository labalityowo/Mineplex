package mineplex.core.report;

/**
 * Contains all possible teams a player can be a member of and it's database mapping.
 */
public enum ReportTeam
{
	RC((short) 0, 30);

	private final short _databaseId;
	private final int _initialPriority;

	ReportTeam(short databaseId, int initialPriority)
	{
		_databaseId = databaseId;
		_initialPriority = initialPriority;
	}

	public String getName()
	{
		return name();
	}

	public short getDatabaseId()
	{
		return _databaseId;
	}

	public int getInitialPriority()
	{
		return _initialPriority;
	}

	public static ReportTeam getById(short databaseId)
	{
		for (ReportTeam team : values())
		{
			if (team.getDatabaseId() == databaseId)
			{
				return team;
			}
		}

		return null;
	}
}
