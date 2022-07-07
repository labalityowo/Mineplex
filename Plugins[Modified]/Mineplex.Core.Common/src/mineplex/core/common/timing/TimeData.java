package mineplex.core.common.timing;

public class TimeData
{
	public TimeData(String title, long time)
	{
		Title = title;
		Started = time;
		LastMarker = time;
		Total = 0L;
	}
	
	public String Title;
	public long Started;
	public long LastMarker;
	public long Total;
	public int Count = 0;
	
	public void addTime()
	{
		Total += System.currentTimeMillis() - LastMarker;
		LastMarker = System.currentTimeMillis();
		Count++;
	}
	
	public void printInfo()
	{
		System.out.println("]==[TIME DATA]==[ " + Count + " " + Title + " took " + Total + "ms in the last " + (System.currentTimeMillis() - Started) + "ms");
	}
}
