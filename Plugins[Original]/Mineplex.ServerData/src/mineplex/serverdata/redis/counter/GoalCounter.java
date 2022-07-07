package mineplex.serverdata.redis.counter;

import mineplex.serverdata.Region;
import mineplex.serverdata.servers.ConnectionData;

import java.util.ArrayList;
import java.util.List;

/**
 * A redis counter that is aiming to reach a goal
 *
 * @author Shaun Bennett
 */
public class GoalCounter extends Counter
{
	private int _lastMilestone;
	// Has the goal been completed?
	private boolean _completed;
	// The goal we are trying to reach
	private long _goal;

	private List<GoalCounterListener> _listeners;

	public GoalCounter(ConnectionData writeConnection, ConnectionData readConnection, Region region, String dataKey, long goal)
	{
		super(writeConnection, readConnection, region, dataKey);

		init(goal);
	}

	public GoalCounter(String dataKey, long goal)
	{
		super(dataKey);

		init(goal);
	}

	private void init(long goal)
	{
		_completed = false;
		_goal = goal;
		_listeners = new ArrayList<>();

		updateCount();

		_lastMilestone = (int) getFillPercent();

		updateMilestone();
	}

	/**
	 * Get the progress towards the goal as a percent ranging from 0 to 1.
	 * @return the percent progress towards goal
	 */
	public double getFillPercent()
	{
		return (((double) getCount()) / _goal);
	}

	/**
	 * Has the goal been completed?
	 * @return
	 */
	public boolean isCompleted()
	{
		return _completed;
	}

	/**
	 * Get the goal for this GoalCounter
	 * @return the goal of this counter
	 */
	public long getGoal()
	{
		return _goal;
	}

	/**
	 * Add a listener to this GoalCounter
	 * @param listener the listener to be added
	 */
	public void addListener(GoalCounterListener listener)
	{
		_listeners.add(listener);
	}

	/**
	 * Clear all the listeners
	 */
	public void clearListeners()
	{
		_listeners.clear();
	}

	/**
	 * Update {@link #_completed} and notify listeners if it has completed
	 */
	private void updateMilestone()
	{
		int currentMilestone = (int) getFillPercent();

		if (currentMilestone != _lastMilestone && currentMilestone > _lastMilestone)
		{
			_listeners.forEach(listener -> listener.onMilestone(this, currentMilestone));
		}

		_lastMilestone = currentMilestone;
	}

	@Override
	protected void updateCount(long newCount)
	{
		super.updateCount(newCount);

		updateMilestone();
	}

}
