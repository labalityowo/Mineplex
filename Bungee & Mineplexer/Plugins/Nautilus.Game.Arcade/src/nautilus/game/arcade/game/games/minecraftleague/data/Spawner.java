package nautilus.game.arcade.game.games.minecraftleague.data;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class Spawner
{
	protected MinecraftLeague Host;
	private Location _spawnerBlock;
	private EntityType _toSpawn;
	private long _lastSpawned;
	private boolean _canSpawn = true;
	
	@SuppressWarnings("deprecation")
	public Spawner(MinecraftLeague host, Location spawnerBlock, EntityType toSpawn)
	{
		Host = host;
		_spawnerBlock = spawnerBlock;
		_toSpawn = toSpawn;
		_lastSpawned = System.currentTimeMillis();
		spawnerBlock.getBlock().setTypeIdAndData(Material.MOB_SPAWNER.getId(), (byte)toSpawn.getTypeId(), false);
	}
	
	private List<Location> getLocations(Location base, int changeX, int changeY, int changeZ)
	{
		List<Location> locs = new ArrayList<Location>();
	    for(int x = (base.getBlockX() - changeX); x <= (base.getBlockX() + changeX); x++)
	    {
	        for(int y = (base.getBlockY() - changeY); y <= (base.getBlockY() + changeY); y++)
	        {
	            for(int z = (base.getBlockZ() - changeZ); z <= (base.getBlockZ() + changeZ); z++)
	            {
	            	Location loc = new Location(base.getWorld(), x, y, z);
	                locs.add(loc);
	            }
	        }
	    }
	    
	    return locs;
	}
	
	private boolean canSpawnMob(Location l)
	{
		Block b = l.getBlock();
		if ((b.getType() != Material.AIR) && !b.getType().toString().contains("WATER") && !UtilBlock.airFoliage(b))
			return false;
		
		Block b2 = b.getRelative(BlockFace.UP);
		if ((b2.getType() != Material.AIR) && !b2.getType().toString().contains("WATER") && !UtilBlock.airFoliage(b2))
			return false;
		
		return true;
	}
	
	private void spawn()
	{
		if (!_canSpawn)
			return;
		
		List<Location> possible = getLocations(_spawnerBlock, 2, 1, 2);
		boolean spawned = false;
		int i = UtilMath.r(possible.size());
		while (!spawned)
		{
			Location l = possible.get(i);
			if (canSpawnMob(l))
			{
				Entity e = Host.getArcadeManager().GetCreature().SpawnEntity(l, _toSpawn);
				UtilEnt.vegetate(e);
				spawned = true;
				_lastSpawned = System.currentTimeMillis();
				continue;
			}
			
			int newi = 0;
			if (i == (possible.size() - 1))
				i = newi;
			else
				i++;
		}
	}
	
	public void update()
	{
		if (_spawnerBlock.getBlock().getType() != Material.MOB_SPAWNER)
			_canSpawn = false;
		else
			_canSpawn = true;
		
		if (_canSpawn)
		{
			if (!UtilTime.elapsed(_lastSpawned, UtilTime.convert(10, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
				_canSpawn = false;
			else
				_canSpawn = true;
		}
		
		spawn();
	}
}
