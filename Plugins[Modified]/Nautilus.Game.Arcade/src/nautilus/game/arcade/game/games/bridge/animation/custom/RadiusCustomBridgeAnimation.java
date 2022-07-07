package nautilus.game.arcade.game.games.bridge.animation.custom;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;

import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;

public class RadiusCustomBridgeAnimation extends CustomBridgeAnimation
{
	
	private double _minDistance;
	
	public RadiusCustomBridgeAnimation(Bridge bridge)
	{
		super(bridge, "RADIUS");
	}
	
	@Override
	public void onParse()
	{
		super.onParse();
		
		_minDistance = _maxDistance;
	}
	
	@Override
	public void onUpdate(UpdateType type)
	{
		if (type != UpdateType.SEC)
		{
			return;
		}

		_minDistance -= _rate;

		Iterator<Location> iterator = _bridgeBlocks.keySet().iterator();

		while (iterator.hasNext())
		{
			Location location = iterator.next();
			double dist = _bridgeBlocks.get(location);

			if (dist > _minDistance)
			{
				Block block = location.getBlock();

				_restore.restore(block);
				onBlockSet(block);
				
				iterator.remove();
			}
		}
	}

}
