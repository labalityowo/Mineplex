package mineplex.core.leaderboard;

import java.util.List;

public abstract class LeaderboardDisplay
{

	protected final LeaderboardManager _manager;

	LeaderboardDisplay(LeaderboardManager manager)
	{
		_manager = manager;
	}

	public abstract void register();

	public abstract void unregister();

	public abstract void update();

	public abstract List<Leaderboard> getDisplayedLeaderboards();
}
