package mineplex.core.scoreboard;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

// fixme bulk send prefix/suffix packets?
public class ScoreboardElement
{
	private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-F]");
	private static final char[] CHARS = "1234567890abcdefklmnor".toCharArray();

	private static final List<String> HAS_COLOR_TRACKERS = new ArrayList<>();

	static
	{
		for (char c : CHARS)
		{
			HAS_COLOR_TRACKERS.add("§" + String.valueOf(c));
		}
	}

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private MineplexScoreboard _scoreboard;
	private Objective _sidebar;

	private ScoreboardLine _line;

	private Team _team;

	private String _tracker;

	private String _customTracker;

	private Set<String> _customTrackerTracker;

	private String _oldValue;

	private int _lineNumber;

	public ScoreboardElement(MineplexScoreboard scoreboard, Objective sidebar, ScoreboardLine line, String tracker, int lineNumber)
	{
		this._scoreboard = scoreboard;
		this._sidebar = sidebar;
		this._line = line;
		this._tracker = tracker;
		this._customTrackerTracker = scoreboard.getCustomTrackers();
		this._team = scoreboard.getHandle().registerNewTeam("SBE" + String.valueOf(COUNTER.getAndIncrement()));
		this._team.addEntry(this._tracker);
		this._lineNumber = lineNumber;
		this._sidebar.getScore(_tracker).setScore(this._lineNumber);
	}

	public void write(int value)
	{
		write(String.valueOf(value));
	}

	public void write(String value)
	{
		if (value.equals(this._oldValue)) return;

		this._oldValue = value;

		// If everything can be fit in the prefix, go ahead and do that
		if (value.length() <= 16)
		{
			if (!StringUtils.equals(this._team.getPrefix(), value))
				_team.setPrefix(value);
			if (!StringUtils.equals(this._team.getSuffix(), ""))
				_team.setSuffix("");

			clearCustomTracker();
		}
		else
		{
			String left = value.substring(0, 16);
			String right = value.substring(16);
			String ending = ChatColor.getLastColors(left);
			right = ending + right;
			// If everything can be fit in the prefix and suffix (don't forget about color codes!), do that
			if (right.length() <= 16)
			{
				if (!StringUtils.equals(this._team.getPrefix(), left))
					_team.setPrefix(left);
				if (!StringUtils.equals(this._team.getSuffix(), right))
					_team.setSuffix(right);

				clearCustomTracker();
			}
			else
			{
				String temp = value.substring(16);
				temp = ending + temp;
				Matcher matcher = COLOR_PATTERN.matcher(ending);
				boolean hasColors = matcher.find();
				if (!hasColors)
				{
					temp = ChatColor.WHITE + temp;
				}

				String tracker = null;
				int index = 0;

				// Determine the most suitable tracker. The scoreboard only has 15 lines so we should never need to append more than 2 characters
				while (tracker == null)
				{
					String temp1 = HAS_COLOR_TRACKERS.get(index++) + temp;
					String substr = temp1.length() <= 40 ? temp1 : temp1.substring(0, 40);
					if (substr.equals(_customTracker) || _customTrackerTracker.add(substr))
					{
						tracker = substr;
						temp = temp1;
					}
				}

				if (_customTracker == null || !_customTracker.equals(tracker))
				{
					clearCustomTracker();
				}

				// If everything can be fit in the tracker, do that
				if (temp.length() <= 40)
				{
					if (this._customTracker == null)
					{
						this._customTracker = temp;

						this._scoreboard.getHandle().resetScores(this._tracker);

						this._team.addEntry(this._customTracker);
						this._sidebar.getScore(this._customTracker).setScore(this._lineNumber);
					}

					if (!StringUtils.equals(this._team.getPrefix(), left))
						this._team.setPrefix(left);
					if (!StringUtils.equals(this._team.getSuffix(), ""))
						this._team.setSuffix("");
				}
				else
				{
					// Otherwise try to use the prefix
					right = temp.substring(40);

					// It's too long for even the suffix. Trim and move on
					if (right.length() > 16)
					{
						right = right.substring(0, 16);
						System.out.println("WARNING: Trimmed suffix from '" + temp.substring(40) + "' to '" + right + "'");
					}

					if (this._customTracker == null)
					{
						this._customTracker = tracker;

						this._scoreboard.getHandle().resetScores(this._tracker);

						this._team.addEntry(this._customTracker);
						this._sidebar.getScore(this._customTracker).setScore(this._lineNumber);
					}

					if (!StringUtils.equals(this._team.getPrefix(), left))
						this._team.setPrefix(left);
					if (!StringUtils.equals(this._team.getSuffix(), right))
						this._team.setSuffix(right);
				}
			}
		}
	}

	protected Team getHandle()
	{
		return this._team;
	}

	protected String getTracker()
	{
		if (this._customTracker != null)
			return this._customTracker;
		return _tracker;
	}

	public void setLineNumber(int lineNumber)
	{
		this._lineNumber = lineNumber;
		this._sidebar.getScore(getTracker()).setScore(this._lineNumber);
	}

	public void delete()
	{
		this._team.unregister();
		this._scoreboard.getHandle().resetScores(this._tracker);
		if (this._customTracker != null)
		{
			this._scoreboard.getHandle().resetScores(this._customTracker);
			_customTrackerTracker.remove(this._customTracker);
		}
		this._scoreboard.returnTracker(this._tracker);
		this._team = null;
		this._scoreboard = null;
		this._tracker = null;
	}

	private void clearCustomTracker()
	{
		if (this._customTracker != null)
		{
			this._scoreboard.getHandle().resetScores(this._customTracker);
			this._team.removeEntry(this._customTracker);
			_customTrackerTracker.remove(this._customTracker);
			this._customTracker = null;

			this._team.addEntry(this._tracker);
			this._sidebar.getScore(this._tracker).setScore(this._lineNumber);
		}
	}
}
