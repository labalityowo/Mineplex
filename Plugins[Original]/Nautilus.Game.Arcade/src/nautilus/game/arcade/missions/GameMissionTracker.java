package nautilus.game.arcade.missions;

import mineplex.core.game.GameDisplay;
import mineplex.core.mission.MissionTracker;
import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;

public class GameMissionTracker<T extends Game> extends MissionTracker
{

	protected final T _game;

	public GameMissionTracker(MissionTrackerType trackerType, T game)
	{
		super(game.getArcadeManager().getMissionsManager(), trackerType);

		_game = game;
	}

	protected GameDisplay getGameType()
	{
		return _game.GetType().getDisplay();
	}
}
