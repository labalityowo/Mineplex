package mineplex.gemhunters.world;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.leaderboard.Leaderboard;
import mineplex.core.leaderboard.LeaderboardManager;
import mineplex.core.leaderboard.LeaderboardRepository.LeaderboardSQLType;
import mineplex.core.leaderboard.StaticLeaderboard;

public class Leaderboards
{

	private static final String STAT_BASE = "Gem Hunters";

	private final LeaderboardManager _manager;
	private final WorldDataModule _worldData;

	public Leaderboards()
	{
		_manager = Managers.require(LeaderboardManager.class);
		_worldData = Managers.require(WorldDataModule.class);

		// Make sure the world is loaded
		UtilServer.runSyncLater(this::createLeaderboards, 20);
	}

	private void createLeaderboards()
	{
		_manager.registerLeaderboard("TOP_GEM_HUNTERS_KILLS", new StaticLeaderboard(
				_manager,
				"Top Kills",
				new Leaderboard(
						LeaderboardSQLType.ALL,
						STAT_BASE + ".Kills"
				),
				_worldData.getCustomLocation("TOP_KILLS").get(0)));
		_manager.registerLeaderboard("TOP_GEM_HUNTERS_DAILY_KILLS", new StaticLeaderboard(
				_manager,
				"Top Daily Kills",
				new Leaderboard(
						LeaderboardSQLType.DAILY,
						STAT_BASE + ".Kills"
				),
				_worldData.getCustomLocation("TOP_DAILY_KILLS").get(0)));
		_manager.registerLeaderboard("TOP_GEM_HUNTERS_GEMS", new StaticLeaderboard(
				_manager,
				"Top Gems Cashed Out",
				new Leaderboard(
						LeaderboardSQLType.ALL,
						STAT_BASE + ".GemsEarned"
				),
				_worldData.getCustomLocation("TOP_GEMS").get(0)));
		_manager.registerLeaderboard("TOP_GEM_HUNTERS_DAILY_GEMS", new StaticLeaderboard(
				_manager,
				"Top Daily Gems Cashed Out",
				new Leaderboard(
						LeaderboardSQLType.DAILY,
						STAT_BASE + ".GemsEarned"
				),
				_worldData.getCustomLocation("TOP_DAILY_GEMS").get(0)));
	}
}