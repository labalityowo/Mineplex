package mineplex.core.common.api;

import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

/**
 * @author Shaun Bennett
 */
public class ApiFieldNamingStrategy implements FieldNamingStrategy
{
	@Override
	public String translateName(Field field)
	{
		return (field.getName().startsWith("_") ? field.getName().substring(1) : field.getName());
	}
}
