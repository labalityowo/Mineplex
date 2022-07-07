package nautilus.game.arcade.missions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;

public class BlocksMissionTracker extends GameMissionTracker<Game>
{

	public BlocksMissionTracker(Game game)
	{
		super(null, game);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event)
	{
		_manager.incrementProgress(event.getPlayer(), 1, MissionTrackerType.GAME_BLOCK_PLACE, getGameType(), event.getBlock().getType());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent event)
	{
		_manager.incrementProgress(event.getPlayer(), 1, MissionTrackerType.GAME_BLOCK_BREAK, getGameType(), event.getBlock().getType());
	}
}
