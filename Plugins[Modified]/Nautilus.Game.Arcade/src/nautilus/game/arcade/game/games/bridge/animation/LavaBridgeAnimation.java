package nautilus.game.arcade.game.games.bridge.animation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;
import nautilus.game.arcade.game.games.bridge.BridgePart;

public class LavaBridgeAnimation extends BridgeAnimation
{

	private List<Location> _lavaSource;
	private List<Location> _lavaBridge;

	public LavaBridgeAnimation(Bridge bridge)
	{
		super(bridge);

		_lavaSource = new ArrayList<>();
		_lavaBridge = new ArrayList<>(AVERAGE_BRIDGE_BLOCKS);
	}

	@Override
	public void onParse()
	{
		for (Location loc : _worldData.GetDataLocs("RED"))
		{
			_lavaBridge.add(loc.getBlock().getLocation());
		}

		for (Location loc : _worldData.GetDataLocs("ORANGE"))
		{
			_lavaBridge.add(loc.getBlock().getLocation());
			_lavaBridge.add(loc.getBlock().getRelative(BlockFace.UP).getLocation());
		}

		_lavaSource = _worldData.GetDataLocs("BLACK");
	}

	@Override
	public void onUpdate(UpdateType type)
	{
		if (type != UpdateType.FASTEST)
		{
			return;
		}

		for (int i = 0; i < 3; i++)
		{
			if (!_lavaBridge.isEmpty() && !_lavaSource.isEmpty())
			{
				// Random Block
				Location bestLoc = _lavaBridge.get(UtilMath.r(_lavaBridge.size()));

				if (bestLoc.getBlock().getRelative(BlockFace.DOWN).isLiquid())
				{
					continue;
				}
				
				_lavaBridge.remove(bestLoc);

				Location source = _lavaSource.get(UtilMath.r(_lavaSource.size()));

				// Create Part
				FallingBlock block = bestLoc.getWorld().spawnFallingBlock(source, Material.NETHERRACK, (byte) 0);
				BridgePart part = new BridgePart(block, bestLoc, true);
				
				registerBridgePart(part);

				// Sound
				source.getWorld().playSound(source, Sound.EXPLODE, 5f * (float) Math.random(), 0.5f + (float) Math.random());
			}
		}
	}

}
