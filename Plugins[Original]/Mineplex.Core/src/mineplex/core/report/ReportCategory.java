package mineplex.core.report;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Contains the reasons a player can be reported for.
 */
public enum ReportCategory
{
	/**
	 * Global category, used for representing values which aren't tied to a specific category (such as abusive report statistics).
	 */
	GLOBAL((short) 0),

	/**
	 * Hacking category, for reports involving cheats of any sort.
	 */
	HACKING((short) 1),

	/**
	 * Chat Abuse category, for reports involving offensive comments made in chat.
	 */
	CHAT_ABUSE((short) 2),

	/**
	 * Gameplay category, for reports specific to gameplay (such as bug exploits or issues with the map).
	 */
	GAMEPLAY((short) 3);

	private final short _id;
	private final String _name;

	ReportCategory(short id)
	{
		_id = id;
		_name = WordUtils.capitalizeFully(name().replace('_', ' '));
	}

	/**
	 * Gets the id, mainly used for database
	 *
	 * @return the id
	 */
	public short getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public static ReportCategory getById(int id)
	{
		for (ReportCategory reportCategory : values())
		{
			if (reportCategory.getId() == id)
			{
				return reportCategory;
			}
		}

		return null;
	}
}
