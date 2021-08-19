package mineplex.core.common.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

public class UtilTime
{
	public static final ZoneId CENTRAL_ZONE = ZoneId.of("America/Chicago"); // This means "CST"
	public static final String DATE_FORMAT_NOW = "MM-dd-yyyy HH:mm:ss";
	public static final String DATE_FORMAT_DAY = "MM-dd-yyyy";
	
	public static String now() 
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	public static String when(long time) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(time);
	}

	
	public static String date() 
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DAY);
		return sdf.format(cal.getTime());
	}
	
	public static String date(long date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DAY);
		return sdf.format(date);
	}

	public static String getDayOfMonthSuffix(final int n)
	{
		if (n >= 11 && n <= 13) {
			return "th";
		}
		switch (n % 10) {
			case 1:  return "st";
			case 2:  return "nd";
			case 3:  return "rd";
			default: return "th";
		}
	}
	
	/**
	 * Converts a {@link Timestamp} to a {@link LocalDateTime}.
	 * This method will only work for timestamp's stored using {@link #CENTRAL_ZONE}, if stored using
	 * another zone please see: {@link #fromTimestamp(Timestamp, ZoneId)}.
	 *
	 * @param timestamp the timestamp to convert
	 * @return the time
	 */
	public static LocalDateTime fromTimestamp(Timestamp timestamp)
	{
		return fromTimestamp(timestamp, CENTRAL_ZONE);
	}

	/**
	 * Converts a {@link Timestamp} to a {@link LocalDateTime}.
	 * The zone supplied should be that of which the timezone was stored using.
	 *
	 * @param timestamp the timestamp to convert
	 * @param zoneId the zone of the timestamp
	 * @return the time
	 */
	public static LocalDateTime fromTimestamp(Timestamp timestamp, ZoneId zoneId)
	{
		return LocalDateTime.ofInstant(timestamp.toInstant(), zoneId);
	}

	/**
	 * Converts a {@link LocalDateTime} to a {@link Timestamp}.
	 * Please not that this will convert using the {@link #CENTRAL_ZONE} timezone.
	 *
	 * @param localDateTime the time to convert
	 * @return the timestamp
	 */
	public static Timestamp toTimestamp(LocalDateTime localDateTime)
	{
		return toTimestamp(localDateTime, CENTRAL_ZONE);
	}

	/**
	 * Converts a {@link LocalDateTime} to a {@link Timestamp}.
	 *
	 * @param localDateTime the time to convert
	 * @param zoneId the zone to use when converting to a timestamp
	 * @return the timestamp
	 */
	public static Timestamp toTimestamp(LocalDateTime localDateTime, ZoneId zoneId)
	{
		return new Timestamp(localDateTime.atZone(zoneId).toInstant().toEpochMilli());
	}

	public enum TimeUnit
	{
		FIT(1),
		DAYS(86400000),
		HOURS(3600000),
		MINUTES(60000),
		SECONDS(1000),
		MILLISECONDS(1);

		private long _ms;

		TimeUnit(long ms)
		{
			_ms = ms;
		}

		public long getMilliseconds()
		{
			return _ms;
		}

		public static TimeUnit[] decreasingOrder()
		{
			return new TimeUnit[]{ DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS };
		}
	}

	/**
	 * Convert from one TimeUnit to a different one
	 */
	public static long convert(long time, TimeUnit from, TimeUnit to)
	{
		long milleseconds = time * from.getMilliseconds();
		return milleseconds / to.getMilliseconds();
	}
	
	public static String since(long epoch)
	{
		return "Took " + convertString(System.currentTimeMillis()-epoch, 1, TimeUnit.FIT) + ".";
	}
	
	public static double convert(long time, int trim, TimeUnit type)
	{
		if (type == TimeUnit.FIT)			
		{
			if (time < 60000)				type = TimeUnit.SECONDS;
			else if (time < 3600000)		type = TimeUnit.MINUTES;
			else if (time < 86400000)		type = TimeUnit.HOURS;
			else							type = TimeUnit.DAYS;
		}
		
		if (type == TimeUnit.DAYS)			return UtilMath.trim(trim, (time)/86400000d);
		if (type == TimeUnit.HOURS)			return UtilMath.trim(trim, (time)/3600000d);
		if (type == TimeUnit.MINUTES)		return UtilMath.trim(trim, (time)/60000d);
		if (type == TimeUnit.SECONDS)		return UtilMath.trim(trim, (time)/1000d);
		else								return UtilMath.trim(trim, time);
	}
	
	public static String MakeStr(long time)
	{
		return convertString(time, 1, TimeUnit.FIT);
	}
	
	public static String MakeStr(long time, int trim)
	{
		return convertString(Math.max(0, time), trim, TimeUnit.FIT);
	}

	public static String convertColonString(long time)
	{
		return convertColonString(time, TimeUnit.HOURS, TimeUnit.SECONDS);
	}

	/**
	 * Converts a time into a colon separated string, displaying max to min units.
	 *
	 * @param time Time in milliseconds
	 * @param max The max {@link TimeUnit} to display, inclusive
	 * @param min The min {@link TimeUnit} to display, inclusive
	 * @return A colon separated string to represent the time
	 */
	public static String convertColonString(long time, TimeUnit max, TimeUnit min)
	{
		if (time == -1)     return "Permanent";
		else if (time == 0) return "0";

		StringBuilder sb = new StringBuilder();
		long curr = time;
		for (TimeUnit unit : TimeUnit.decreasingOrder())
		{
			if (unit.getMilliseconds() >= min.getMilliseconds() && unit.getMilliseconds() <= max.getMilliseconds())
			{
				long amt = curr / unit.getMilliseconds();
				if (amt < 10 && unit.getMilliseconds() != max.getMilliseconds()) sb.append('0'); // prefix single digit numbers with a 0
				sb.append(amt);
				if (unit.getMilliseconds() > min.getMilliseconds()) sb.append(':');
				curr -= amt * unit.getMilliseconds();
			}
		}

		return sb.toString();
	}
	
	public static String convertString(long time, int trim, TimeUnit type)
	{
		if (time <= -1)						return "Permanent";
		
		if (type == TimeUnit.FIT)			
		{
			if (time < 60000)				type = TimeUnit.SECONDS;
			else if (time < 3600000)		type = TimeUnit.MINUTES;
			else if (time < 86400000)		type = TimeUnit.HOURS;
			else							type = TimeUnit.DAYS;
		}
		
		String text;
		double num;
		if (trim == 0)
		{
			if (type == TimeUnit.DAYS)			text = (num = UtilMath.trim(trim, time / 86400000d)) + " Day";
			else if (type == TimeUnit.HOURS)	text = (num = UtilMath.trim(trim, time / 3600000d)) + " Hour";
			else if (type == TimeUnit.MINUTES)	text = (int) (num = (int) UtilMath.trim(trim, time / 60000d)) + " Minute";
			else if (type == TimeUnit.SECONDS)	text = (int) (num = (int) UtilMath.trim(trim, time / 1000d)) + " Second"; 
			else								text = (int) (num = (int) UtilMath.trim(trim, time)) + " Millisecond";
		}
		else
		{
			if (type == TimeUnit.DAYS)			text = (num = UtilMath.trim(trim, time / 86400000d)) + " Day";
			else if (type == TimeUnit.HOURS)	text = (num = UtilMath.trim(trim, time / 3600000d)) + " Hour";
			else if (type == TimeUnit.MINUTES)	text = (num = UtilMath.trim(trim, time / 60000d)) + " Minute";
			else if (type == TimeUnit.SECONDS)	text = (num = UtilMath.trim(trim, time / 1000d)) + " Second";
			else								text = (int) (num = (int) UtilMath.trim(0, time)) + " Millisecond";
		}
		
		if (num != 1)
		    text += "s";
		
		return text;
	}
	
	public static boolean elapsed(long from, long required)
	{
		return System.currentTimeMillis() - from > required;
	}
}
