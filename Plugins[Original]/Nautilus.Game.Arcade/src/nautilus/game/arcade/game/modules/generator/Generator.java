package nautilus.game.arcade.game.modules.generator;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;

public class Generator
{

	private static final int COLLECT_RADIUS = 2;
	private static final float ROTATION_DELTA_YAW = 10;

	private final GeneratorType _type;
	private final Location _location;
	private final Block _block;

	private ArmorStand _holder;
	private long _lastCollect;
	private boolean _colourTick = true;

	public Generator(GeneratorType type, Location location)
	{
		_type = type;
		_location = location.clone().subtract(0, 0.5, 0);
		_block = location.getBlock().getRelative(BlockFace.DOWN);
	}

	public void checkCollect()
	{
		if (_holder == null)
		{
			return;
		}

		List<Player> nearby = UtilPlayer.getNearby(_location, COLLECT_RADIUS);

		if (nearby.isEmpty())
		{
			return;
		}

		Player player = nearby.get(0);
		_type.collect(this, player);
		_holder.remove();
		_holder = null;
		setLastCollect();
		UtilServer.CallEvent(new GeneratorCollectEvent(player, this));
	}

	public void checkSpawn()
	{
		if (_holder != null || !UtilTime.elapsed(_lastCollect, _type.getSpawnRate()))
		{
			return;
		}

		_holder = _type.spawnHolder(this);
	}

	public void animateHolder()
	{
		if (_holder == null)
		{
			return;
		}

		Location location = _holder.getLocation();

		((CraftEntity) _holder).getHandle().setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw() + ROTATION_DELTA_YAW, location.getPitch());
	}

	public void updateName()
	{
		if (_holder == null)
		{
			return;
		}

		if (!_holder.isCustomNameVisible())
		{
			_holder.setCustomNameVisible(true);
		}

		if (_type.isFlashName())
		{
			_colourTick = !_colourTick;
		}

		_holder.setCustomName((_colourTick ? _type.getColour() + C.Bold : C.cWhiteB) + _type.getName());
	}

	public GeneratorType getType()
	{
		return _type;
	}

	public ArmorStand getHolder()
	{
		return _holder;
	}

	public Location getLocation()
	{
		return _location;
	}

	public Block getBlock()
	{
		return _block;
	}

	public void setLastCollect()
	{
		_lastCollect = System.currentTimeMillis();
	}

	public long getTimeUtilSpawn()
	{
		return _lastCollect + _type.getSpawnRate() - System.currentTimeMillis();
	}
}
