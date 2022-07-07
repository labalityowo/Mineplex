package nautilus.game.arcade.game.modules;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.world.WorldData;

public class MapCrumbleModule extends Module
{

	private final Set<Block> _worldBlocks;

	private boolean _enabled;
	private long _enableAfter;
	private Runnable _onCrumble;
	private int _rate = 4;

	public MapCrumbleModule()
	{
		_worldBlocks = new HashSet<>(100000);
	}

	@Override
	protected void setup()
	{
		WorldData worldData = getGame().WorldData;

		getGame().getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			int y = worldData.MinY;

			@Override
			public void run()
			{
				if (!getGame().equals(getGame().getArcadeManager().GetGame()))
				{
					cancel();
					return;
				}

				World world = worldData.World;

				for (int x = worldData.MinX; x < worldData.MaxX; x++)
				{
					for (int z = worldData.MinZ; z < worldData.MaxZ; z++)
					{
						Block block = world.getBlockAt(x, y, z);

						if (block.getType() != Material.AIR)
						{
							_worldBlocks.add(block);
						}
					}
				}

				if (y++ == worldData.MaxY)
				{
					cancel();
				}
			}
		}, 1, 2);
	}

	@Override
	public void cleanup()
	{
		_worldBlocks.clear();
	}

	public MapCrumbleModule setEnabled(boolean enabled)
	{
		_enabled = enabled;
		return this;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public MapCrumbleModule setEnableAfter(long enableAfter, Runnable onCrumble)
	{
		_enableAfter = enableAfter;
		_onCrumble = onCrumble;
		return this;
	}

	public MapCrumbleModule setRate(int rate)
	{
		_rate = rate;
		return this;
	}

	public long getTimeUntilCrumble()
	{
		return getGame().GetStateTime() + _enableAfter - System.currentTimeMillis();
	}

	private void addWorldBlock(Block block)
	{
		if (block.getWorld().equals(getGame().WorldData.World))
		{
			_worldBlocks.add(block);
		}
	}

	@EventHandler
	public void crumbleStart(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !getGame().IsLive() || _enabled || !UtilTime.elapsed(getGame().GetStateTime(), _enableAfter))
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
		if (event.getType() != UpdateType.TICK || !getGame().IsLive() || _worldBlocks.isEmpty() || !_enabled)
		{
			return;
		}

		Location center = getGame().GetSpectatorLocation();

		for (int i = 0; i < _rate; i++)
		{
			Block bestBlock = null;
			double bestDist = 0;

			for (Block block : _worldBlocks)
			{
				double dist = UtilMath.offset2dSquared(center, block.getLocation().add(0.5, 0, 0.5));

				if (bestBlock == null || dist > bestDist)
				{
					bestBlock = block;
					bestDist = dist;
				}
			}

			if (bestBlock == null)
			{
				continue;
			}

			while (bestBlock.getRelative(BlockFace.DOWN).getType() != Material.AIR)
			{
				bestBlock = bestBlock.getRelative(BlockFace.DOWN);
			}

			_worldBlocks.remove(bestBlock);

			if (!bestBlock.getWorld().equals(center.getWorld()) || bestBlock.getType() == Material.AIR)
			{
				continue;
			}

			if (bestBlock.getType() == Material.WOODEN_DOOR || bestBlock.getType() == Material.IRON_DOOR_BLOCK)
			{
				MapUtil.QuickChangeBlockAt(bestBlock.getRelative(BlockFace.UP).getLocation(), Material.AIR);
			}
			else if (Math.random() > 0.95)
			{
				bestBlock.getWorld().spawnFallingBlock(bestBlock.getLocation().add(0.5, 0.5, 0.5), bestBlock.getType(), bestBlock.getData());
			}

			MapUtil.QuickChangeBlockAt(bestBlock.getLocation(), Material.AIR);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event)
	{
		addWorldBlock(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockBreakEvent event)
	{
		_worldBlocks.remove(event.getBlock());
	}
}
