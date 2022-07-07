package nautilus.game.arcade.game.games.bridge.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;
import nautilus.game.arcade.game.games.bridge.BridgePart;

public class WoodBridgeAnimation extends BridgeAnimation
{

	private static final BlockFace[] FACES = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	private static final int Y_MOD = 30;

	private final Map<Location, Material> _woodBridge;

	public WoodBridgeAnimation(Bridge bridge)
	{
		super(bridge);

		_woodBridge = new HashMap<>(AVERAGE_BRIDGE_BLOCKS);
	}

	@Override
	public void onParse()
	{
		onParse(_worldData.GetDataLocs("BROWN"), false);
		onParse(_worldData.GetDataLocs("GRAY"), true);
	}

	public void onParse(List<Location> locations, boolean aboveBelow)
	{
		for (Location location : locations)
		{
			if (aboveBelow)
			{
				_woodBridge.put(location.getBlock().getRelative(BlockFace.UP).getLocation(), Material.FENCE);
				_woodBridge.put(location, Material.LOG);
			}
			else
			{
				_woodBridge.put(location, Material.WOOD_STEP);
			}
		}
	}

	@Override
	public void onUpdate(UpdateType type)
	{
		if (type != UpdateType.FASTEST)
		{
			return;
		}
		
		List<Location> toDo = new ArrayList<>();

		for (Location location : _woodBridge.keySet())
		{
			Material material = _woodBridge.get(location);
			
			if (material == Material.LOG)
			{
				int adjacent = 0;

				for (BlockFace face : FACES)
				{
					if (!isAir(location.getBlock().getRelative(face)))
					{
						adjacent++;
					}
				}
				if (adjacent > 0)
				{
					toDo.add(location);
				}
			}
			else if (material == Material.FENCE)
			{
				if (!isAir(location.getBlock().getRelative(BlockFace.DOWN)))
				{
					toDo.add(location);
				}
			}
			else if (material == Material.WOOD_STEP)
			{
				int adjacent = 0;

				for (BlockFace face : FACES)
				{
					if (!isAir(location.getBlock().getRelative(face)))
					{
						adjacent++;
					}
				}
				if (adjacent > 0)
				{
					toDo.add(location);
				}
			}
		}

		if (toDo.isEmpty())
		{
			return;
		}

		for (Location location : toDo)
		{
			Material material = _woodBridge.remove(location);
			Location source = location.clone().add(0, Y_MOD, 0);

			// Create Part
			FallingBlock block = location.getWorld().spawnFallingBlock(source, material, (byte) 0);
			block.setVelocity(new Vector(0, -1, 0));
			BridgePart part = new BridgePart(block, location, false);
			
			registerBridgePart(part);
		}
	}
}
