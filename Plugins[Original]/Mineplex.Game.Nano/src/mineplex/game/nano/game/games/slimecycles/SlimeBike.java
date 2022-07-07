package mineplex.game.nano.game.games.slimecycles;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;

public class SlimeBike
{

	private static final long DIRECTION_RATE = 500;
	private static final double DELTA_TICK = 0.4;

	private final SlimeCycles _game;
	private final Player _host;
	private final byte _colour;
	private final Slime _bike;
	private final List<Location> _trail;

	private Block _lastBlock;
	private float _lastYaw;
	private long _lastChange;
	private boolean _changeDirection;

	SlimeBike(SlimeCycles game, Player host, DyeColor colour)
	{
		_game = game;
		_host = host;
		_colour = colour.getWoolData();
		_bike = host.getWorld().spawn(host.getLocation(), Slime.class);
		_trail = new ArrayList<>();

		_bike.setSize(2);
		UtilEnt.vegetate(_bike, true);
		UtilEnt.ghost(_bike, true, false);
		UtilEnt.setFakeHead(_bike, true);

		_lastBlock = _bike.getLocation().getBlock();
		_lastYaw = -1;
	}

	void updateDirection()
	{
		if (UtilTime.elapsed(_lastChange, DIRECTION_RATE))
		{
			float yaw = (Math.round(_host.getLocation().getYaw() / 90F) & 0x3) * 90F;

			if (_lastYaw != yaw)
			{
				_lastYaw = yaw;
				_lastChange = System.currentTimeMillis();
				_changeDirection = true;
			}
		}
	}

	boolean updateLocation()
	{
		Location location = _bike.getLocation();
		Block block = location.getBlock();

		if (block.getType() != Material.AIR)
		{
			return true;
		}

		double x = location.getX();
		double z = location.getZ();
		double deltaX = 0;
		double deltaZ = 0;

		if (_lastYaw == 0)
		{
			deltaZ += DELTA_TICK;
		}
		else if (_lastYaw == 90)
		{
			deltaX -= DELTA_TICK;
		}
		else if (_lastYaw == 180)
		{
			deltaZ -= DELTA_TICK;
		}
		else
		{
			deltaX += DELTA_TICK;
		}

		if (!block.equals(_lastBlock))
		{
			if (_trail.size() >= _game.getTrailSize())
			{
				Location remove = _trail.remove(0);

				MapUtil.QuickChangeBlockAt(remove, Material.AIR);
			}

			Location lastBlock = _lastBlock.getLocation();

			_trail.add(lastBlock);
			MapUtil.QuickChangeBlockAt(lastBlock, Material.STAINED_GLASS_PANE, _colour);

			_lastBlock = block;
		}

		if (_changeDirection)
		{
			boolean canChange = false;

			if (deltaZ != 0 && x - location.getBlockX() > 0.5)
			{
				x = location.getBlockX() + 0.5;
				canChange = true;
			}
			else if (deltaX != 0 && z - location.getBlockZ() > 0.5)
			{
				z = location.getBlockZ() + 0.5;
				canChange = true;
			}

			if (canChange)
			{
				_changeDirection = false;
				UtilEnt.CreatureLook(_bike, _lastYaw);
			}
		}

		location.setX(x + deltaX);
		location.setZ(z + deltaZ);

		UtilEnt.setPosition(_bike, location);
		return false;
	}

	void clean()
	{
		_bike.remove();
		_trail.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
		_trail.clear();
	}

	public Slime getEntity()
	{
		return _bike;
	}
}
