package nautilus.game.arcade.game.games.bridge.animation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.games.bridge.Bridge;
import nautilus.game.arcade.game.games.bridge.BridgePart;
import nautilus.game.arcade.world.WorldData;

public abstract class BridgeAnimation
{

	public static final int AVERAGE_BRIDGE_BLOCKS = 3000;

	protected final Bridge _bridge;
	protected final WorldData _worldData;

	public BridgeAnimation(Bridge bridge)
	{
		_bridge = bridge;
		_worldData = bridge.WorldData;
	}

	public abstract void onParse();

	public abstract void onUpdate(UpdateType type);

	public void registerBridgePart(BridgePart part)
	{
		_bridge.getBridgeParts().add(part);
	}

	public boolean isAir(Location location)
	{
		return isAir(location.getBlock());
	}

	public boolean isAir(Block block)
	{
		return block.getType() == Material.AIR;
	}

}
