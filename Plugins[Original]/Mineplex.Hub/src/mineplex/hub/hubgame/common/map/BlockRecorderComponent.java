package mineplex.hub.hubgame.common.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import mineplex.core.common.Pair;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;

public class BlockRecorderComponent extends HubGameComponent<CycledGame>
{

	private final Map<Location, Pair<Material, Byte>> _blocks;
	private final Set<Location> _placedBlocks;
	private final Location _cornerA;
	private final Location _cornerB;

	public BlockRecorderComponent(CycledGame game, Location cornerA, Location cornerB)
	{
		super(game);

		_placedBlocks = new HashSet<>();
		_blocks = new HashMap<>(500);

		for (Block block : UtilBlock.getInBoundingBox(cornerA, cornerB, false))
		{
			_blocks.put(block.getLocation(), Pair.create(block.getType(), block.getData()));
		}

		_cornerA = cornerA;
		_cornerB = cornerB;
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		Location location = event.getBlock().getLocation();

		if (!_game.isLive() || !_game.isAlive(player) || !_placedBlocks.contains(location))
		{
			return;
		}

		event.setCancelled(false);
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		Location location = event.getBlock().getLocation();

		if (!_game.isLive() || !_game.isAlive(player) || !UtilAlg.inBoundingBox(location, _cornerA, _cornerB))
		{
			return;
		}

		_placedBlocks.add(location);
		event.setCancelled(false);
	}
	
	@EventHandler
	public void end(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.Waiting || !event.getGame().equals(_game))
		{
			return;
		}

		_game.getManager().runSyncLater(() -> _blocks.forEach((location, pair) -> MapUtil.QuickChangeBlockAt(location, pair.getLeft(), pair.getRight())), 1);
		_placedBlocks.clear();
	}
}
