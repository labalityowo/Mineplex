package mineplex.core.boosters;

import com.mojang.authlib.properties.PropertyMap;
import mineplex.core.common.util.UtilTime;

import java.util.Date;
import java.util.UUID;

/**
 * @author Shaun Bennett
 */
public class Booster
{
	private int _id;
	private String _playerName;
	private UUID _uuid;
	private int _accountId;
	private int _duration;
	private double _multiplier;
	private Date _startTime;
	private Date _endTime;
	private Date _activationTime;
//	private PropertyMap _propertyMap;

	public Booster()
	{
	}

	public int getId()
	{
		return _id;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public UUID getUuid()
	{
		return _uuid;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public int getDuration()
	{
		return _duration;
	}

	public Date getStartTime()
	{
		return _startTime;
	}

	public Date getEndTime()
	{
		return _endTime;
	}

	public Date getActivationTime()
	{
		return _activationTime;
	}

	public boolean isActive()
	{
		Date now = new Date();
		return getStartTime().before(now) && getEndTime().after(now);
	}

	public long getTimeRemaining()
	{
		if (isActive())
		{
			return Math.max(0, getEndTime().getTime() - System.currentTimeMillis());
		}
		else if (getEndTime().after(new Date()))
		{
			return _duration * 1000L;
		}
		else
		{
			return 0;
		}
	}

	public String getTimeRemainingString()
	{
		return UtilTime.convertColonString(getTimeRemaining(), UtilTime.TimeUnit.MINUTES, UtilTime.TimeUnit.SECONDS);
	}

	public double getMultiplier()
	{
		return _multiplier;
	}

//	public PropertyMap getPropertyMap()
//	{
//		return _propertyMap;
//	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Booster booster = (Booster) o;

		if (_id != booster._id) return false;
		return _accountId == booster._accountId;
	}

	@Override
	public int hashCode()
	{
		int result = _id;
		result = 31 * result + _accountId;
		return result;
	}

	public int getIncreasePercent()
	{
		return (int) (getMultiplier() - 1) * 100;
	}

	@Override
	public String toString()
	{
		return "Booster{" +
				"_id=" + _id +
				", _playerName='" + _playerName + '\'' +
				", _uuid=" + _uuid +
				", _accountId=" + _accountId +
				", _duration=" + _duration +
				", _multiplier=" + _multiplier +
				", _startTime=" + _startTime +
				", _endTime=" + _endTime +
				", _activationTime=" + _activationTime +
				'}';
	}
}
