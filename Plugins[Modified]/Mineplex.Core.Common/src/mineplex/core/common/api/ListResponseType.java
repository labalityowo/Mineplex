package mineplex.core.common.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Shaun Bennett
 */
public class ListResponseType<Data> implements ParameterizedType
{
	private Class<Data> _wrapped;

	public ListResponseType(Class<Data> wrapped)
	{
		_wrapped = wrapped;
	}

	@Override
	public Type[] getActualTypeArguments()
	{
		return new Type[] { _wrapped };
	}

	@Override
	public Type getRawType()
	{
		return List.class;
	}

	@Override
	public Type getOwnerType()
	{
		return null;
	}
}
