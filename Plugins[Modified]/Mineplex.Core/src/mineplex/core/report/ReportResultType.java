package mineplex.core.report;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Contains all possible outcomes for a report.
 */
public enum ReportResultType
{
	ACCEPTED((short) 0, false),
	DENIED((short) 1, false),
	ABUSIVE((short) 2, true),
	EXPIRED((short) 3, true);

	private final short _id;
	private final boolean _globalStat;
	private final String _name;

	ReportResultType(short id, boolean globalStat)
	{
		_id = id;
		_globalStat = globalStat;
		_name = WordUtils.capitalizeFully(name().replace('_', ' '));
	}

	public short getId()
	{
		return _id;
	}

	public boolean isGlobalStat()
	{
		return _globalStat;
	}

	public String getName()
	{
		return _name;
	}

	public static ReportResultType getById(int id)
	{
		for (ReportResultType resultType : values())
		{
			if (resultType.getId() == id)
			{
				return resultType;
			}
		}

		return null;
	}
}
