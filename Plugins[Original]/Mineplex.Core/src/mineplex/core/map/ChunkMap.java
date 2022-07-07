package mineplex.core.map;

import net.minecraft.server.v1_8_R3.WorldMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ChunkMap
{
	public ItemStack getItem(Location loc)
	{
		MapView map = Bukkit.createMap(Bukkit.getWorlds().get(0));

		for (MapRenderer r : map.getRenderers())
		{
			map.removeRenderer(r);
		}

		ItemStack item = new ItemStack(Material.MAP);

		item.setDurability(map.getId());

		WorldMap worldMap = new WorldMap("map_" + map.getId());

		ChunkMapRenderer renderer = new ChunkMapRenderer(worldMap);
		
		map.addRenderer(renderer);
		
		worldMap.scale=(byte)3;
		worldMap.centerX = loc.getBlockX();
		worldMap.centerZ = loc.getBlockZ();
		worldMap.c();
		
		renderer.setupMap(loc.getWorld());

		return item;
	}
}
