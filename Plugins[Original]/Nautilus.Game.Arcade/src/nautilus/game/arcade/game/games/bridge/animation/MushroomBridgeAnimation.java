package nautilus.game.arcade.game.games.bridge.animation;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;

public class MushroomBridgeAnimation extends BridgeAnimation
{

	private Map<Location, Long> _mushroomStem = new HashMap<>();
	private Map<Location, Long> _mushroomTop = new HashMap<>();
	private boolean _stemsGrown = false;

	public MushroomBridgeAnimation(Bridge bridge)
	{
		super(bridge);
	}

	@Override
	public void onParse()
	{
		for (Location loc : _worldData.GetCustomLocs("21"))
		{
			_mushroomStem.put(loc, 0L);
			loc.getBlock().setType(Material.AIR);
		}
		
		for (Location loc : _worldData.GetDataLocs("PURPLE"))
		{
			_mushroomTop.put(loc, 0L);
		}
	}

	@Override
	public void onUpdate(UpdateType type)
	{
		if (_mushroomStem != null && !_mushroomStem.isEmpty())
		{
			for (int i = 0; i < 4 && !_mushroomStem.isEmpty(); i++)
			{
				double lowestY = 0;
				Location lowestLoc = null;

				for (Location loc : _mushroomStem.keySet())
				{
					if (!UtilTime.elapsed(_mushroomStem.get(loc), 6000))
						continue;

					if (lowestLoc == null || loc.getY() < lowestY)
					{
						lowestY = loc.getY();
						lowestLoc = loc;
					}
				}

				if (lowestLoc == null)
					continue;

				_mushroomStem.remove(lowestLoc);

				MapUtil.QuickChangeBlockAt(lowestLoc, 100, (byte) 15);
			}
		}
		else
		{
			_stemsGrown = true;
		}

		if (_stemsGrown && _mushroomTop != null && !_mushroomTop.isEmpty())
		{
			int attempts = 0;
			int done = 0;
			while (done < 6 && attempts < 400)
			{
				attempts++;

				// Random Block
				Location loc = UtilAlg.Random(_mushroomTop.keySet());

				if (!UtilTime.elapsed(_mushroomTop.get(loc), 6000))
					continue;

				Block block = loc.getBlock();

				if (block.getRelative(BlockFace.DOWN).getType() == Material.AIR && block.getRelative(BlockFace.NORTH).getType() == Material.AIR && block.getRelative(BlockFace.EAST).getType() == Material.AIR && block.getRelative(BlockFace.SOUTH).getType() == Material.AIR && block.getRelative(BlockFace.WEST).getType() == Material.AIR)
					continue;

				_mushroomTop.remove(loc);

				MapUtil.QuickChangeBlockAt(block.getLocation(), 99, (byte) 14);

				done++;
			}
		}
	}

}
