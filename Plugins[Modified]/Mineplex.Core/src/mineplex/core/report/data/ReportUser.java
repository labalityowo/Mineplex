package mineplex.core.report.data;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.TreeBasedTable;
import mineplex.core.report.ReportCategory;
import mineplex.core.report.ReportResultType;
import mineplex.core.report.ReportTeam;

/**
 * Holds report specific data for a user.
 * This includes player reputation
 */
public class ReportUser
{
	private final static TreeBasedTable<ReportCategory, ReportResultType, Integer> RESULT_WORTH = TreeBasedTable.create();

	static {
		RESULT_WORTH.put(ReportCategory.GLOBAL, ReportResultType.ABUSIVE, -20);
		RESULT_WORTH.put(ReportCategory.GLOBAL, ReportResultType.ACCEPTED, 10);
		RESULT_WORTH.put(ReportCategory.CHAT_ABUSE, ReportResultType.DENIED, -5);
		RESULT_WORTH.put(ReportCategory.HACKING, ReportResultType.DENIED, -1);
		RESULT_WORTH.put(ReportCategory.GAMEPLAY, ReportResultType.DENIED, -1);
	}

	private final int _accountId;
	private final Set<ReportTeam> _teams = new HashSet<>();
	private final Map<ReportCategory, Map<ReportResultType, Integer>> _reputation;
	
	public ReportUser(int accountId)
	{
		_accountId = accountId;
		_reputation = Collections.synchronizedMap(new EnumMap<>(ReportCategory.class)); // allows map to be modified in database thread

		for (ReportCategory category : ReportCategory.values())
		{
			Map<ReportResultType, Integer> resultValues = new EnumMap<>(ReportResultType.class);

			for (ReportResultType resultType : ReportResultType.values())
			{
				// ensure global stats are only applied to GLOBAL and non-global stats are not applied to GLOBAL
				if (resultType.isGlobalStat() == (category == ReportCategory.GLOBAL))
				{
					resultValues.put(resultType, 0);
				}
			}

			_reputation.put(category, resultValues);
		}
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public Set<ReportTeam> getTeams()
	{
		return _teams;
	}

	public void addTeam(ReportTeam team)
	{
		_teams.add(team);
	}

	public void removeTeam(ReportTeam team)
	{
		_teams.remove(team);
	}

	public boolean hasTeam(ReportTeam team)
	{
		return _teams.contains(team);
	}

	/**
	 * Sets the value of a result type within the specified category.
	 *
	 * @param category the category type
	 * @param resultType the result type
	 * @param newValue the new value
	 */
	public void setValue(ReportCategory category, ReportResultType resultType, int newValue)
	{
		_reputation.get(category).put(resultType, newValue);
	}

	/**
	 * Gets the value of a result type within the specified category.
	 *
	 * @param category the category type
	 * @param  resultType the result type
	 * @return the value
	 */
	public int getValue(ReportCategory category, ReportResultType resultType)
	{
		Map<ReportResultType, Integer> resultValues = _reputation.get(category);

		if (resultValues == null || !resultValues.containsKey(resultType))
		{
			throw new UnsupportedOperationException("Cannot retrieve " + resultType.name() + " value for category " + category.name() + " (no global value either).");
		}

		return resultValues.get(resultType);
	}

	/**
	 * Gets the reputation of this user for the specified category.
	 * This takes into account the amount of accepted, denied and abusive reports the user has made.
	 *
	 * @param category the category to get the reputation for
	 * @return the reputation
	 */
	public int getReputation(ReportCategory category)
	{
		int accepted = getReputationPart(category, ReportResultType.ACCEPTED);
		int denied = getReputationPart(category, ReportResultType.DENIED);
		int abusive = getReputationPart(ReportCategory.GLOBAL, ReportResultType.ABUSIVE);

		return Math.max(accepted + denied + abusive, 1);
	}

	/**
	 * Gets the users reputation for the specified category and result type.
	 *
	 * @param category the category
	 * @param resultType the result type
	 * @return the reputation
	 */
	private int getReputationPart(ReportCategory category, ReportResultType resultType)
	{
		return getValue(category, resultType) * getResultWorth(category, resultType);
	}

	/**
	 * Gets how much a result is worth for the category provided.
	 * If no worth value is found for the specified category, the value for {@link ReportCategory#GLOBAL} will be returned.
	 *
	 * @param reportCategory the category
	 * @param resultType the result type
	 * @return the worth value
	 */
	private static int getResultWorth(ReportCategory reportCategory, ReportResultType resultType)
	{
		if (RESULT_WORTH.contains(reportCategory, resultType))
		{
			return RESULT_WORTH.get(reportCategory, resultType);
		}
		else
		{
			return RESULT_WORTH.get(ReportCategory.GLOBAL, resultType);
		}
	}
}
