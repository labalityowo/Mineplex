package mineplex.core.updater;

import mineplex.core.common.util.UtilTime;

public enum UpdateType  
{
	/**
	 * Once every 64 minutes
	 */
	MIN_64(3840000),
	/**
	 * Once every 60 minutes
	 */
	HOUR_01(3600000),
	/**
	 * Once every 32 minutes
	 */
	MIN_32(1920000),
	/**
	 * Once every 30 minutes
	 */
	MIN_30(1800000),
	/**
	 * Once every 16 minutes
	 */
	MIN_16(960000),
	/**
	 * Once every 10 minutes
	 */
	MIN_10(600000),
	/**
	 * Once every 8 minutes
	 */
	MIN_08(480000),
	/**
	 * Once every 5 minutes
	 */
	MIN_05(300000),
	/**
	 * Once every 4 minutes
	 */
	MIN_04(240000),
	/**
	 * Once every 2 minutes
	 */
	MIN_02(120000),
	/**
	 * Once every minute
	 */
	MIN_01(60000),
	/**
	 * Once every 32 seconds
	 */
	SLOWEST(32000),
	/**
	 * Once every 30 seconds
	 */
	SEC_30(30000),
	/**
	 * Once every 20 seconds
	 */
	SEC_20(20000),
	/**
	 * Once every 16 seconds
	 */
	SLOWER(16000),
	/**
	 * Once every 8 seconds
	 */
	SEC_08(8000),
	/**
	 * Once every 5 seconds
	 */
	SEC_05(5000),
	/**
	 * Once every 4 seconds
	 */
	SLOW(4000),
	/**
	 * Once every 40 ticks
	 */
	TWOSEC(2000),
	/**
	 * Once every 20 ticks
	 */
	SEC(1000),
	/**
	 * Once every 10 ticks
	 */
	FAST(500),
	/**
	 * Once every 5 ticks
	 */
	FASTER(250),
	/**
	 * Once every 3 ticks
	 */
	FASTEST(125),
	/**
	 * Once every tick
	 */
	TICK(49);

	private long _time;
	private long _last;
	private long _timeSpent;
	private long _timeCount;
	
	UpdateType(long time)
	{
		_time = time;
		_last = System.currentTimeMillis();
	}
	
	public boolean Elapsed()
	{
		if (UtilTime.elapsed(_last, _time))
		{
			_last = System.currentTimeMillis();
			return true;
		}
		
		return false;
	}
	
	public long getTicksTillNextCall()
	{
		long diff = System.currentTimeMillis()-_last;
		if(diff >= _time) return 0;
		return -diff/50;
	}

	public void StartTime()
	{
		_timeCount = System.currentTimeMillis();
	}

	public void StopTime()
	{
		_timeSpent += System.currentTimeMillis() - _timeCount;
	}

	public void PrintAndResetTime()
	{
		System.out.println(this.name() + " in a second: " + _timeSpent);
		_timeSpent = 0;
	}

	public long getMilliseconds()
	{
		return _time;
	}
}
