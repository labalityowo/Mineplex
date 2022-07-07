package nautilus.game.arcade.game.games.bridge.animation;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;

public class LillyPadBridgeAnimation extends BridgeAnimation
{

	private Map<Location, Long> _lillyPads = new HashMap<>();

	public LillyPadBridgeAnimation(Bridge bridge)
	{
		super(bridge);
	}

	@Override
	public void onParse()
	{
		for (Location loc : _worldData.GetDataLocs("LIME"))
		{
			_lillyPads.put(loc, 0L);
		}
	}

	@Override
	public void onUpdate(UpdateType type)
	{
		if (type != UpdateType.FASTEST)
			return;

		for (int i = 0; i < 3; i++)
		{
			if (_lillyPads != null && !_lillyPads.isEmpty())
			{
				// Random Block
				Location loc = UtilAlg.Random(_lillyPads.keySet());

				if (!UtilTime.elapsed(_lillyPads.get(loc), 8000))
					continue;

				if (!loc.getBlock().getRelative(BlockFace.DOWN).isLiquid())
					continue;

				_lillyPads.remove(loc);

				MapUtil.QuickChangeBlockAt(loc, Material.WATER_LILY);

				// Sound
				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 111);
			}
		}
	}

}
