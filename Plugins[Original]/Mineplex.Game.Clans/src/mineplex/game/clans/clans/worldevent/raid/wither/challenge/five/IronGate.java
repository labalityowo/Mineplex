package mineplex.game.clans.clans.worldevent.raid.wither.challenge.five;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.EventCreature;

public class IronGate
{
	private ChallengeFive _challenge;
	private List<Location> _corners;
	private List<EventCreature<?>> _guardians;
	
	public IronGate(ChallengeFive challenge, List<Location> corners, List<EventCreature<?>> guardians)
	{
		_challenge = challenge;
		_corners = corners;
		_guardians = guardians;
	}
	
	private void open()
	{
		Location pos1 = _corners.get(0);
		Location pos2 = _corners.get(1);
		int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
		int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
		int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
		int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + 3;
		int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
		int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
		
		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					Block block = _challenge.getRaid().getWorldData().World.getBlockAt(x, y, z);
					ClansManager.getInstance().getBlockRestore().restore(block);
					if (block.getType() == Material.IRON_FENCE)
					{
						block.setType(Material.AIR);
					}
				}
			}
		}
	}
	
	public Location getCenter()
	{
		Location pos1 = _corners.get(0);
		Location pos2 = _corners.get(1);
		int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
		int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
		int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
		int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + 3;
		int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
		int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
		
		return new Location(_challenge.getRaid().getWorldData().World, minX + ((maxX - minX) / 2), minY + ((maxY - minY) / 2), minZ + ((maxZ - minZ) / 2));
	}
	
	public boolean update()
	{
		if (_guardians.size() < 1)
		{
			open();
			return true;
		}
		return false;
	}
	
	public void handleDeath(EventCreature<?> creature)
	{
		_guardians.remove(creature);
	}
}