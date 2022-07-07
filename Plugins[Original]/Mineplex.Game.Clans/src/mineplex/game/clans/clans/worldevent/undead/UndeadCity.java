package mineplex.game.clans.clans.worldevent.undead;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.api.EventState;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.game.clans.clans.worldevent.undead.creature.UndeadArcher;
import mineplex.game.clans.clans.worldevent.undead.creature.UndeadWarrior;

public class UndeadCity extends WorldEvent
{

	private final int _maxChests;
	private final int _maxMobs;
	private final Map<BlockPosition, CityChest> _chests = new HashMap<>();
	private final List<Location> _spawnSpots = new ArrayList<>();
	private long _lastSpawn;
	
	public UndeadCity(WorldEventManager manager)
	{
		super("Undead City", UndeadCityLocation.getRandomLocation().toLocation(UtilWorld.getWorld("world")), 55, true, manager.getDisguiseManager(), manager.getClans().getProjectile(), manager.getDamage(), manager.getBlockRestore(), manager.getClans().getCondition());
		
		_maxChests = UndeadCityLocation.getLastLocation().getMaxChests();
		_maxMobs = UndeadCityLocation.getLastLocation().getMaxMobs();
	}
	
	@Override
	public boolean allowsIcePrison()
	{
		return true;
	}
	
	@Override
	protected void customStart()
	{
		int addedChests = 0;
		int addedMobs = 0;
		double size = getEventArena().getRadius();

		for (Block block : UtilBlock.getInBoundingBox(getCenterLocation().clone().add(size, size, size), getCenterLocation().clone().subtract(size, size, size)))
		{
			if (block.getType() == Material.ENDER_CHEST)
			{
				BlockPosition position = new BlockPosition(block);
				CityChest chest = new CityChest(block, addedChests++ < _maxChests);
				_chests.put(position, chest);
			}
		}
		for (int i = 0; i < (_maxMobs + 10); i++)
		{
			Location loc = ClansManager.getInstance().getWorldEvent().getTerrainFinder().locateSpace(getCenterLocation(), 55, 0, 3, 0, false, true, new HashSet<>());
			if (loc == null)
			{
				continue;
			}
			_spawnSpots.add(loc);
			if (addedMobs++ < _maxMobs)
			{
				if (ThreadLocalRandom.current().nextInt(2) == 0)
				{
					registerCreature(new UndeadArcher(this, loc));
				}
				else
				{
					registerCreature(new UndeadWarrior(this, loc));
				}
			}
		}
		
		_lastSpawn = System.currentTimeMillis();
	}
	
	@Override
	protected void customTick()
	{
		if (getState() != EventState.LIVE)
		{
			return;
		}
		int active = 0;
		for (CityChest chest : _chests.values())
		{
			if (chest.isEnabled() && !chest.isOpen())
			{
				active++;
			}
		}
		if (active < 1)
		{
			stop();
			return;
		}
		if (UtilTime.elapsed(_lastSpawn, 15000) && getCreatures().size() < Math.min(_maxMobs, _spawnSpots.size()))
		{
			Location loc = UtilMath.randomElement(_spawnSpots);
			if (ThreadLocalRandom.current().nextInt(2) == 0)
			{
				registerCreature(new UndeadArcher(this, loc));
			}
			else
			{
				registerCreature(new UndeadWarrior(this, loc));
			}
			
			_lastSpawn = System.currentTimeMillis();
		}
	}
	
	@Override
	public void customCleanup(boolean onDisable)
	{
		_chests.values().forEach(CityChest::revert);
	}

	@Override
	protected void customStop()
	{
		_chests.clear();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChestOpen(PlayerInteractEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()) || event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		if (!event.hasBlock())
		{
			return;
		}
		if (!UtilEvent.isAction(event, ActionType.ANY))
		{
			return;
		}
		BlockPosition block = new BlockPosition(event.getClickedBlock());
		if (_chests.containsKey(block))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You smash open an " + F.elem("Undead Chest") + "!"));
			_chests.get(block).open();
		}
	}
	
	private static class BlockPosition
	{
		private final int _x, _y, _z;
		
		public BlockPosition(Block block)
		{
			_x = block.getX();
			_y = block.getY();
			_z = block.getZ();
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(_x, _y, _z);
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o instanceof BlockPosition)
			{
				BlockPosition pos = (BlockPosition) o;
				return pos._x == _x && pos._y == _y && pos._z == _z;
			}
			
			return false;
		}
	}
}