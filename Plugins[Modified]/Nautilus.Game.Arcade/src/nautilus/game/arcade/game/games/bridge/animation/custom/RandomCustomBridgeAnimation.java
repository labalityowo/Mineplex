package nautilus.game.arcade.game.games.bridge.animation.custom;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;

import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;

public class RandomCustomBridgeAnimation extends CustomBridgeAnimation
{

	public RandomCustomBridgeAnimation(Bridge bridge)
	{
		super(bridge, "RANDOM");
	}

	@Override
	public void onUpdate(UpdateType type)
	{
		if (type != UpdateType.TICK)
		{
			return;
		}

		Iterator<Location> iterator = _bridgeBlocks.keySet().iterator();
		int i = 0;

		while (iterator.hasNext() && i < _rate)
		{
			i++;
			Location location = iterator.next();

			Block block = location.getBlock();

			_restore.restore(block);
			onBlockSet(block);

			iterator.remove();
		}
	}

}
