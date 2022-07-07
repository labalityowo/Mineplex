package mineplex.core.achievement;

import mineplex.core.game.GameDisplay;

/**
 * The purpose of extracting stats to this class is so we can display stats that are a combination
 * of different stat values. For example, since we don't have a specific stat for games played of a game,
 * we can use this class to display the stat "Games Played" that sums up "Wins" and "Losses"
 * See: StatDisplay.GAMES_PLAYED
 */
public class StatDisplay
{
	private String _displayName;
	private String[] _stats;
	private boolean _fullStat;
	private boolean _justDisplayName;
	private boolean _divideStats;

	public StatDisplay(String stat)
	{
		this(stat, false);
	}

	public StatDisplay(String stat, boolean justDisplayName)
	{
		_displayName = stat;
		_stats = new String[] { stat };
		_fullStat = false;
		_divideStats = false;
		_justDisplayName = justDisplayName;
	}
	
	public StatDisplay(String displayName, boolean divideStats, String... stats)
	{
		this(displayName, false, divideStats, stats);
	}

	public StatDisplay(String displayName, String... stats)
	{
		this(displayName, false, false, stats);
	}

	public StatDisplay(String displayName, boolean fullStat, boolean divideStats, String... stats)
	{
		_displayName = displayName;
		_stats = stats;
		_fullStat = fullStat;
		_divideStats = divideStats;
	}

	public String getDisplayName()
	{
		return _displayName;
	}

	public boolean isJustDisplayName()
	{
		return _justDisplayName;
	}

	public String[] getStats()
	{
		return _stats;
	}

	public boolean isFullStat()
	{
		return _fullStat;
	}
	
	public boolean isDivideStats()
	{
		return _divideStats;
	}

	public static StatDisplay fromGame(String name, GameDisplay gameDisplay, String... stats)
	{
		String[] formattedStats = new String[stats.length];
		for (int i = 0; i < stats.length; i++)
		{
			formattedStats[i] = gameDisplay.getName() + "." + stats[i];
		}

		return new StatDisplay(name, true, false, formattedStats);
	}

	public static final StatDisplay WINS = new StatDisplay("Wins");
	public static final StatDisplay LOSSES = new StatDisplay("Losses");
	public static final StatDisplay KILLS = new StatDisplay("Kills");
	public static final StatDisplay DEATHS = new StatDisplay("Deaths");
	public static final StatDisplay GEMS_EARNED = new StatDisplay("Gems Earned", "GemsEarned");
	public static final StatDisplay CROWNS_EARNED = new StatDisplay("Crowns Earned", "CrownsEarned");
	public static final StatDisplay TIME_IN_GAME = new StatDisplay("Time In Game", "TimeInGame");
	public static final StatDisplay GAMES_PLAYED = new StatDisplay("Games Played", "Wins", "Losses");

}
