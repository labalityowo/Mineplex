package mineplex.core.scoreboard;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 * This is a Mineplex Scoreboard, featuring new and improved Anti Flicker™ technology
 *
 * To use, you must first define some {@link ScoreboardLine}s. Each ScoreboardLine must
 *   be unique and implement {@link java.lang.Object#equals} and {@link java.lang.Object#hashCode}.
 *   ScoreboardLines are only considered equal if both {@link java.lang.Object#equals} and {@link java.lang.Object#hashCode} are equal
 *
 * Once you've defined your ScoreboardLines, simply register them using {@link #register}. If you wish to remove a line, simply use {@link #unregister}
 *
 * Once you have finished registering and unregistering your lines, call {@link #recalculate} to push all the line changes to the actual scoreboard.
 *   Please note that you must call {@link #recalculate} before doing any writing
 *
 * Once the line changes have been pushed, you can begin writing. First, get the {@link ScoreboardElement} by calling {@link #get} with your ScoreboardLine
 *   Then, with the ScoreboardElement, call {@link ScoreboardElement#write} with a string of length up to 72 characters, including color codes
 *
 * Please note that some restrictions apply. If you have two line that are the same when substringed 16-56, then only one can be added
 *   Most of the time the implementation will deal with collisions, but if it is unable to an {@link IllegalArgumentException} will be raised
 * </pre>
 */
public class MineplexScoreboard
{
	// The list of legal trackers (we use color codes followed by a reset to hide them)
	private static final char[] CHARS = "1234567890abcdefklmnor".toCharArray();

	// The owner
	private final Player _owner;

	// The underlying scoreboard
	private final Scoreboard _scoreboard;

	// The sidebar
	private final Objective _sidebar;

	// The list of available trackers, implemented as a queue
	private final LinkedList<String> _availableTrackers = new LinkedList<>();
	// The set of custom trackers
	private final Set<String> _customTrackers = new HashSet<>();

	// The list of registered lines, which have been calculated, in the order of registration
	// The ScoreboardLine at index 0 is the one at the top of the scoreboard
	private final List<ScoreboardLine> _calculated = new ArrayList<>();
	// The map of registered lines to their writable elements
	private final Map<ScoreboardLine, ScoreboardElement> _calculatedMap = new HashMap<>();

	// The list of buffered lines
	private final List<ScoreboardLine> _buffered = new ArrayList<>();

	/**
	 * Creates a MineplexScoreboard with no owner
	 */
	public MineplexScoreboard()
	{
		this(null);
	}

	/**
	 * Creates a MineplexScoreboard with the given owner
	 */
	public MineplexScoreboard(Player owner)
	{
		_owner = owner;
		_scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		_sidebar = _scoreboard.registerNewObjective("sidebar", "sidebar");
		_sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (char c : CHARS)
		{
			_availableTrackers.add(String.valueOf(ChatColor.COLOR_CHAR) + String.valueOf(c) + ChatColor.RESET.toString());
		}
	}

	/**
	 * Registers the given {@link ScoreboardLine}, placing it at the bottom of the scoreboard, so to speak
	 *
	 * @param line The ScoreboardLine to register
	 * @returns The same instance, to allow chaining
	 */
	public MineplexScoreboard register(ScoreboardLine line)
	{
		_buffered.add(line);
		return this;
	}

	/**
	 * Registers the given {@link ScoreboardLine} {@code line}, but only after the ScoreboardLine {@code after}
	 *
	 * @param line The ScoreboardLine to register
	 * @param after The ScoreboardLine to register {@code line} after
	 * @return The same instance, to allow chaining
	 */
	public MineplexScoreboard registerAfter(ScoreboardLine line, ScoreboardLine after)
	{
		int index = _buffered.indexOf(after);
		if (index == -1)
		{
			throw new IllegalStateException("Could not locate line: " + after);
		}
		_buffered.add(index, line);
		return this;
	}

	/**
	 * Unregisters the given {@link ScoreboardLine} from this scoreboard
	 *
	 * @param line The ScoreboardLine to unregister
	 * @return The same instance, to allow chaining
	 */
	public MineplexScoreboard unregister(ScoreboardLine line)
	{
		_buffered.remove(line);
		return this;
	}

	/**
	 * Recalculates and pushes line changes to the {@link org.bukkit.scoreboard.Scoreboard}
	 */
	public void recalculate()
	{
		// Starting fresh
		if (_calculated.size() == 0)
		{
			for (int i = 0; i < _buffered.size() && !_availableTrackers.isEmpty(); i++)
			{
				String tracker = _availableTrackers.pop();
				ScoreboardLine line = _buffered.get(i);
				_calculated.add(line);
				_calculatedMap.put(line, new ScoreboardElement(this, _sidebar, line, tracker, _buffered.size() - i));
			}
		}
		// Otherwise compute deltas
		else
		{
			// Delete all removed lines
			for (ScoreboardLine calculated : _calculated)
			{
				if (!_buffered.contains(calculated))
				{
					ScoreboardElement element = get(calculated);
					element.delete();
				}
			}

			Map<ScoreboardLine, ScoreboardElement> prevCalculatedMap = new HashMap<>(_calculatedMap);

			_calculated.clear();
			_calculatedMap.clear();

			for (int i = 0; i < _buffered.size(); i++)
			{
				ScoreboardLine line = _buffered.get(i);
				ScoreboardElement element = prevCalculatedMap.get(line);
				int expectedScore = _buffered.size() - i;

				// existing
				if (element != null)
				{
					Score score = _sidebar.getScore(element.getTracker());
					if (score.getScore() != expectedScore)
					{
						score.setScore(expectedScore);
						element.setLineNumber(expectedScore);
					}

					_calculated.add(line);
					_calculatedMap.put(line, element);
				}
				// new
				else
				{
					String tracker = _availableTrackers.pop();
					_calculated.add(line);
					_calculatedMap.put(line, new ScoreboardElement(this, _sidebar, line, tracker, expectedScore));
				}
			}
		}

		_buffered.clear();
		_buffered.addAll(_calculated);
	}

	/**
	 * Determines whether a {@link ScoreboardLine} is currently registered or not
	 *
	 * @param line The ScoreboardLine to check
	 * @return True if {@code line} is registered, and false if not
	 */
	public boolean isRegistered(ScoreboardLine line)
	{
		return _calculatedMap.containsKey(line);
	}

	/**
	 * Gets the {@link ScoreboardElement} associated with the {@link ScoreboardLine}
	 *
	 * @param line The registered ScoreboardLine
	 *
	 * @return The ScoreboardElement, which can be written to
	 */
	public ScoreboardElement get(ScoreboardLine line)
	{
		return _calculatedMap.get(line);
	}

	/**
	 * Gets the owner of this scoreboard
	 *
	 * @return The owner, or null if no owner was provided
	 */
	public Player getOwner()
	{
		return _owner;
	}

	/**
	 * Sets the sidebar, or objective, name. This will be not be buffered, but updated instantly
	 *
	 * @param sidebarName The new String to be written to the sidebar
	 */
	public void setSidebarName(String sidebarName)
	{
		if (!StringUtils.equals(_sidebar.getName(), sidebarName))
		{
			_sidebar.setDisplayName(sidebarName);
		}
	}

	/**
	 * Gets the underlying {@link org.bukkit.scoreboard.Scoreboard} instance, because you're a hacky person
	 *
	 * @return The Scoreboard that you want to screw around with
	 */
	public Scoreboard getHandle()
	{
		return _scoreboard;
	}

	void returnTracker(String tracker)
	{
		_availableTrackers.add(tracker);
	}

	Set<String> getCustomTrackers()
	{
		return _customTrackers;
	}
}