package mineplex.game.nano.game.games.minekart;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.game.nano.game.games.minekart.KartController.DriftDirection;

public class Kart
{

	private final Player _driver;
	private final Sheep _vehicle;

	private int _lap, _lapCheckpoint, _lapKeyCheckpoint;

	private Vector _velocity, _offset;
	private float _frontWaysInput, _sidewaysInput;
	private float _yaw;
	private DriftDirection _driftDirection;
	private float _driftPower;
	private long _driftLast;

	private boolean _crashed;
	private long _crashedAt;

	private boolean _resetting;

	private long _boostAt;

	private long _completedAt;

	Kart(Player driver)
	{
		_driver = driver;

		Location location = driver.getLocation();
		_vehicle = driver.getWorld().spawn(location, Sheep.class);

		UtilEnt.vegetate(_vehicle, true);
		UtilEnt.ghost(_vehicle, true, false);
		UtilEnt.setFakeHead(_vehicle, true);

		if (driver.isOp())
		{
			_vehicle.setCustomName("jeb_");
		}
		else
		{
			_vehicle.setColor(UtilMath.randomElement(DyeColor.values()));
		}

		_vehicle.setPassenger(driver);

		_lap = 1;

		_velocity = new Vector();
		_offset = new Vector();
		_yaw = location.getYaw();
	}

	public void remove()
	{
		_vehicle.remove();
	}

	public Player getDriver()
	{
		return _driver;
	}

	public Sheep getVehicle()
	{
		return _vehicle;
	}

	public Location getParticleLocation()
	{
		Location location = _vehicle.getLocation().add(0, 0.3, 0);
		return location.subtract(location.getDirection().multiply(0.4));
	}

	public void setLap(int lap)
	{
		_lap = lap;
	}

	public int getLap()
	{
		return _lap;
	}

	public void setLapCheckpoint(int lapCheckpoint)
	{
		_lapCheckpoint = lapCheckpoint;
	}

	public int getLapCheckpoint()
	{
		return _lapCheckpoint;
	}

	public void setLapKeyCheckpoint(int lapKeyCheckpoint)
	{
		_lapKeyCheckpoint = lapKeyCheckpoint;
	}

	public int getLapKeyCheckpoint()
	{
		return _lapKeyCheckpoint;
	}

	public void setVelocity(Vector velocity)
	{
		_velocity = velocity;
	}

	public Vector getVelocity()
	{
		return _velocity;
	}

	public Vector getOffset()
	{
		return _offset;
	}

	public void setInput(float frontWaysInput, float sidewaysInput)
	{
		_frontWaysInput = frontWaysInput;
		_sidewaysInput = sidewaysInput;
	}

	public float getFrontWaysInput()
	{
		return _frontWaysInput;
	}

	public float getSidewaysInput()
	{
		return _sidewaysInput;
	}

	public void setYaw(float yaw)
	{
		_yaw = yaw;
	}

	public float getYaw()
	{
		return _yaw;
	}

	public void setDriftDirection(DriftDirection driftDirection)
	{
		_driftDirection = driftDirection;
	}

	public void setDriftPower(float driftPower)
	{
		_driftPower = Math.min(0.999F, driftPower);
	}

	public DriftDirection getDriftDirection()
	{
		return _driftDirection;
	}

	public float getDriftPower()
	{
		return _driftPower;
	}

	public void setDriftLast()
	{
		_driftLast = System.currentTimeMillis();
		_driftPower = 0;
	}

	public boolean canBoost()
	{
		return UtilTime.elapsed(_driftLast, 500);
	}

	public boolean isBoosting()
	{
		return !UtilTime.elapsed(_boostAt, 1000) || !UtilTime.elapsed(_driftLast, 1200);
	}

	public void setBoost()
	{
		_boostAt = System.currentTimeMillis();
	}

	public void setCrashed(boolean crashed)
	{
		_crashed = crashed;
		_driftDirection = null;
		_driftPower = 0;
		_driftLast = 0;

		if (crashed)
		{
			_crashedAt = System.currentTimeMillis();
		}
	}

	public boolean isCrashed()
	{
		return _crashed;
	}

	public long getCrashedAt()
	{
		return _crashedAt;
	}

	public void setResetting(boolean resetting)
	{
		_resetting = resetting;
	}

	public boolean isResetting()
	{
		return _resetting;
	}

	public void complete()
	{
		_completedAt = System.currentTimeMillis();
	}

	public long getCompletedAt()
	{
		return _completedAt;
	}
}
