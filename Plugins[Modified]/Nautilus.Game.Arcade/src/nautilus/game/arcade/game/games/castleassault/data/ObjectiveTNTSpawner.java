package nautilus.game.arcade.game.games.castleassault.data;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;

public class ObjectiveTNTSpawner 
{
	private static final long TNT_SPAWN_DELAY = 60000;
	private List<Location> _locs;
	private Location _lastSpawnLoc;
	private Item _entity;
	private long _lastPickedUp;
	
	public ObjectiveTNTSpawner(List<Location> locs)
	{
		_locs = locs;
		_lastSpawnLoc = null;
		_entity = null;
		_lastPickedUp = System.currentTimeMillis();
	}
	
	public Item getItem()
	{
		return _entity;
	}
	
	public boolean isSpawned()
	{
		return _entity != null;
	}
	
	public boolean canPlaceFireAt(Block block)
	{
		for (Location loc : _locs)
		{
			if (UtilMath.offsetSquared(loc, block.getLocation()) <= 9)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public long getNextTNT()
	{
		return (_lastPickedUp + TNT_SPAWN_DELAY) - System.currentTimeMillis();
	}
	
	public void spawn()
	{
		Location spawn = _locs.get(UtilMath.r(_locs.size()));
		spawn.getBlock().getRelative(BlockFace.DOWN).setType(Material.REDSTONE_BLOCK);
		_lastSpawnLoc = spawn.clone();
		_entity = spawn.getWorld().dropItem(spawn, new ItemStack(Material.TNT));
		UtilFirework.playFirework(spawn, Type.BURST, Color.RED, false, false);
	}
	
	public void pickup()
	{
		_entity.getLocation().getBlock().getRelative(BlockFace.DOWN).setType(Material.IRON_BLOCK);
		_entity.remove();
		_entity = null;
		_lastSpawnLoc = null;
		_lastPickedUp = System.currentTimeMillis();
	}
	
	public void update()
	{
		if (!isSpawned() && UtilTime.elapsed(_lastPickedUp, TNT_SPAWN_DELAY))
		{
			spawn();
		}
		else if (isSpawned())
		{
			_entity.teleport(_lastSpawnLoc);
			if (!_entity.isValid() || _entity.isDead())
			{
				_entity = _lastSpawnLoc.getWorld().dropItem(_lastSpawnLoc, new ItemStack(Material.TNT));
			}
		}
	}
	
	public void onStart()
	{
		_lastPickedUp = System.currentTimeMillis();
	}
}