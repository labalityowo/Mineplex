package mineplex.core.common.timing;

import java.util.Map.Entry;

import mineplex.core.common.util.NautHashMap;

public class TimingManager
{
	private static NautHashMap<String, Long> _timingList = new NautHashMap<String, Long>();
	private static NautHashMap<String, TimeData> _totalList = new NautHashMap<String, TimeData>();
	
	private static Object _timingLock = new Object();
	private static Object _totalLock = new Object();
	
	public static boolean Debug = true;
	
	public static void startTotal(String title)
	{
		if (!Debug)
			return;
		
		synchronized(_totalLock)
		{
			if (_totalList.containsKey(title))
			{
				TimeData data = _totalList.get(title);
				data.LastMarker = System.currentTimeMillis();
				
				_totalList.put(title, data);
			}
			else
			{
				TimeData data = new TimeData(title, System.currentTimeMillis());
				_totalList.put(title, data);
			}
		}
	}
	
	public static void stopTotal(String title)
	{
		if (!Debug)
			return;
		
		synchronized(_totalLock)
		{
			if (_totalList.containsKey(title))
			{
				_totalList.get(title).addTime();
			}
		}
	}
	
	public static void printTotal(String title)
	{
		if (!Debug)
			return;
		
		synchronized(_totalLock)
		{
			_totalList.get(title).printInfo();
		}
	}
	
	public static void endTotal(String title, boolean print)
	{
		if (!Debug)
			return;
		
		synchronized(_totalLock)
		{
			TimeData data = _totalList.remove(title);
			
			if (data != null && print)
				data.printInfo();
		}
	}
	
	public static void printTotals()
	{
		if (!Debug)
			return;
		
		synchronized(_totalLock)
		{
			for (Entry<String, TimeData> entry : _totalList.entrySet())
			{
				entry.getValue().printInfo();
			}
		}
	}
	
	public static void start(String title)
	{
		if (!Debug)
			return;
		
		synchronized(_timingLock)
		{
			_timingList.put(title, System.currentTimeMillis());
		}
	}
	
	public static void stop(String title)
	{
		if (!Debug)
			return;
		
		synchronized(_timingLock)
		{
			System.out.println("]==[TIMING]==[" + title + " took " + (System.currentTimeMillis() - _timingList.get(title)) + "ms");
			_timingList.remove(title);
		}
	}
}
