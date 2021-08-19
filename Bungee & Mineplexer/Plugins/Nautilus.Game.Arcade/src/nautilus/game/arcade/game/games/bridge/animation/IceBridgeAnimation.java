package nautilus.game.arcade.game.games.bridge.animation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;

public class IceBridgeAnimation extends BridgeAnimation
{

	private List<Location> _iceBridge;

	public IceBridgeAnimation(Bridge bridge)
	{
		super(bridge);

		_iceBridge = new ArrayList<>(AVERAGE_BRIDGE_BLOCKS);
	}

	@Override
	public void onParse()
	{
		_iceBridge = _worldData.GetDataLocs("LIGHT_BLUE");
	}

	@Override
	public void onUpdate(UpdateType type)
	{
		if (type != UpdateType.FASTEST || _iceBridge.isEmpty())
		{
			return;
		}
		
		int attempts = 0;
		int done = 0;

		while (done < 5 && attempts < 400)
		{
			attempts++;

			// Random Block
			Location loc = UtilAlg.Random(_iceBridge);

			Block block = loc.getBlock().getRelative(BlockFace.DOWN);

			if (!block.isLiquid())
			{
				continue;
			}

			if (block.getRelative(BlockFace.NORTH).isLiquid() && block.getRelative(BlockFace.EAST).isLiquid() && block.getRelative(BlockFace.SOUTH).isLiquid() && block.getRelative(BlockFace.WEST).isLiquid())
			{
				continue;
			}

			_iceBridge.remove(loc);

			if (Math.random() > 0.25)
			{
				MapUtil.QuickChangeBlockAt(block.getLocation(), Material.PACKED_ICE);
			}
			else
			{
				MapUtil.QuickChangeBlockAt(block.getLocation(), Material.ICE);
			}

			done++;
		}
	}

}
