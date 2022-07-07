package mineplex.core.stats;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents a player's statistic information. This object is thread-safe
 */
@ThreadSafe
public class PlayerStats 
{
	private final Object _lock = new Object();

	@GuardedBy("_lock")
	private Map<String, Long> _stats = new HashMap<>();
	
	private final boolean _temporary;
	
	public PlayerStats(boolean temporary)
	{
		_temporary = temporary;
	}
	
	public boolean isTemporary()
	{
		return _temporary;
	}

	/**
	 * Add a value to the specified stat
	 *
	 * @param statName The name of the stat
	 * @param value The value, must be positive
	 * @return The new value for the specified stat
	 */
	long addStat(String statName, long value)
	{
		synchronized (_lock)
		{
			return _stats.merge(statName, Math.max(0, value), Long::sum);
		}
	}

	/**
	 * Sets the value of the specified stat
	 *
	 * @param statName The name of the stat
	 * @param value The value, must be positive
	 * @return The new value for the specified stat
	 */
	long setStat(String statName, long value)
	{
		synchronized (_lock)
		{
			_stats.put(statName, value);
			return value;
		}
	}

	/**
	 * Gets the value for the specified stat
	 *
	 * @param statName The name of the stat
	 * @return The value of the stat if it exists, or 0 if it does not
	 */
	public long getStat(String statName) 
	{
		synchronized (_lock)
		{
			return _stats.getOrDefault(statName, 0L);
		}
	}

	/**
	 * Returns a view of the all the stats. This view will not be updated
	 */
	public Map<String, Long> getStats()
	{
		synchronized (_lock)
		{
			// make it unmodifiable so that people who try to edit it will get an exception instead of silently failing
			return Collections.unmodifiableMap(new HashMap<>(_stats));
		}
	}
}