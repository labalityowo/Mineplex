package mineplex.core.common.util;

import java.lang.reflect.Field;

public class UtilReflection
{
	/**
	 * Returns the value of the field from the given object instance 
	 */
	public static Object getValueOfField(Object object, String fieldName)
	{
		return getValueOfField(object.getClass(), object, fieldName);
	}
	
	/**
	 * Returns the value of the field from the given object instance 
	 */
	public static Object getValueOfField(Class<?> className, Object object, String fieldName)
	{
		try
		{
			Field f = className.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(object);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
