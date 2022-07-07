package mineplex.game.nano.game.games.microbattle.components;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.Pair;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;

public class MapCourruptionComponent extends GameComponent<Game>
{

	private static final Material CORRUPTION_BLOCK = Material.NETHERRACK;

	private final List<Block> _worldBlocks;

	private boolean _enabled;
	private long _enableAfter;
	private Runnable _onCrumble;
	private int _rate = 4;
	private int _y;
	private boolean _sorted;

	public MapCourruptionComponent(Game game)
	{
		super(game, GameState.Prepare, GameState.Live);

		_worldBlocks = new ArrayList<>(20000);
	}

	@Override
	public void disable()
	{
		_worldBlocks.clear();
	}

	public MapCourruptionComponent setEnabled(boolean enabled)
	{
		_enabled = enabled;
		return this;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public MapCourruptionComponent setEnableAfter(long enableAfter, Runnable onCrumble)
	{
		_enableAfter = enableAfter;
		_onCrumble = onCrumble;
		return this;
	}

	public MapCourruptionComponent setRate(int rate)
	{
		_rate = rate;
		return this;
	}

	public long getTimeUntilCrumble()
	{
		return getGame().getStateTime() + _enableAfter - System.currentTimeMillis();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		_y = getGame().getMineplexWorld().getMin().getBlockY();
	}

	@EventHandler
	public void parseMap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || _y > getGame().getMineplexWorld().getMax().getBlockY())
		{
			return;
		}

		MineplexWorld mineplexWorld = getGame().getMineplexWorld();
		World world = mineplexWorld.getWorld();
		int minX = mineplexWorld.getMin().getBlockX(), maxX = mineplexWorld.getMax().getBlockX();
		int minZ = mineplexWorld.getMin().getBlockZ(), maxZ = mineplexWorld.getMax().getBlockZ();

		for (int x = minX; x < maxX; x++)
		{
			for (int z = minZ; z < maxZ; z++)
			{
				Block block = world.getBlockAt(x, _y, z);

				if (block.getType() == Material.AIR || block.isLiquid())
				{
					continue;
				}

				_worldBlocks.add(block);
			}
		}

		_y++;
	}

	private void addWorldBlock(Block block)
	{
		if (block.getWorld().equals(getGame().getMineplexWorld().getWorld()))
		{
			_sorted = false;
			_worldBlocks.add(block);
		}
	}

	@EventHandler
	public void crumbleStart(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !getGame().isLive() || _enabled || !UtilTime.elapsed(getGame().getStateTime(), _enableAfter))
		{
			return;
		}

		_enabled = true;

		if (_onCrumble != null)
		{
			_onCrumble.run();
		}
	}

	@EventHandler
	public void crumble(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !getGame().isLive() || _worldBlocks.isEmpty() || !_enabled)
		{
			return;
		}

		if (!_sorted)
		{
			Location mid = getGame().getSpectatorLocation();

			_worldBlocks.sort((o1, o2) ->
			{
				if (o1.getX() == o2.getX() && o1.getZ() == o2.getZ())
				{
					return Integer.compare(o1.getY(), o2.getY());
				}

				return Double.compare(UtilMath.offsetSquared(mid, o2.getLocation().add(0.5, 0.5, 0.5)), UtilMath.offsetSquared(mid, o1.getLocation().add(0.5, 0.5, 0.5)));
			});
			_sorted = true;
		}

		for (int i = 0; i < _rate; i++)
		{
			if (_worldBlocks.isEmpty())
			{
				return;
			}

			Block bestBlock = _worldBlocks.remove(0);
			Material material = bestBlock.getType();

			if (material == Material.AIR || material == CORRUPTION_BLOCK)
			{
				continue;
			}

			if (material == Material.WOODEN_DOOR || material == Material.IRON_DOOR_BLOCK)
			{
				MapUtil.QuickChangeBlockAt(bestBlock.getRelative(BlockFace.UP).getLocation(), Material.AIR);
				MapUtil.QuickChangeBlockAt(bestBlock.getLocation(), Material.AIR);
				continue;
			}

			MapUtil.QuickChangeBlockAt(bestBlock.getLocation(), CORRUPTION_BLOCK);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event)
	{
		addWorldBlock(event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void blockPlace(BlockBreakEvent event)
	{
		Block block = event.getBlock();

		if (block.getType() == CORRUPTION_BLOCK)
		{
			event.setCancelled(true);
		}
		else
		{
			_worldBlocks.remove(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPhysics(BlockPhysicsEvent event)
	{
		addWorldBlock(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void entityChangeBlock(EntityChangeBlockEvent event)
	{
		addWorldBlock(event.getBlock());
	}

	@EventHandler
	public void corruptionDamage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isEnabled())
		{
			return;
		}

		playerLoop:
		for (Player player : getGame().getAlivePlayers())
		{
			Pair<Location, Location> box = UtilEnt.getSideStandingBox(player);
			Location min = box.getLeft(), max = box.getRight();

			for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
			{
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
				{
					Block block = player.getLocation().add(x, -0.5, z).getBlock();

					if (block.getType() == CORRUPTION_BLOCK)
					{
						getGame().getManager().getDamageManager().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 2, false, true, true, getGame().getGameType().getName(), "Corruption");
						continue playerLoop;
					}
				}
			}
		}
	}
}
