package mineplex.core.common.util;

public class UtilSystem
{
	public static void printStackTrace()
	{
		for (StackTraceElement trace : Thread.currentThread().getStackTrace())
		{	
			System.out.println(trace.toString());
		}	
	}
	
	public static void printStackTrace(StackTraceElement[] stackTrace)
	{
		for (StackTraceElement trace : stackTrace)
		{	
			System.out.println(trace.toString());
		}	
	}
}
