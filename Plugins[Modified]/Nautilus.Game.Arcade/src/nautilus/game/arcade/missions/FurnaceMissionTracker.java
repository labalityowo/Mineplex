package nautilus.game.arcade.missions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceExtractEvent;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;

public class FurnaceMissionTracker extends GameMissionTracker<Game>
{

	public FurnaceMissionTracker(Game game)
	{
		super(MissionTrackerType.GAME_FURNACE_SMELT, game);
	}

	@EventHandler
	public void furnaceExtract(FurnaceExtractEvent event)
	{
		_manager.incrementProgress(event.getPlayer(), event.getItemAmount(), _trackerType, getGameType(), event.getItemType());
	}
}
