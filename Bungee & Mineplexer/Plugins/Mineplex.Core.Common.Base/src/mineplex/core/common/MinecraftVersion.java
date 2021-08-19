package mineplex.core.common;

import com.google.common.base.Preconditions;

/**
 * Used to declare specific milestone versions
 */
public enum MinecraftVersion 
{
	Version1_13("1.13", 393),
	Version1_9("1.9", 48),
	Version1_8("1.8", Integer.MIN_VALUE), //Any player will at minimum be 1.8, we can't handle anything below that
	;
	
	private final String _friendlyName;
	private final int _minimum;

	MinecraftVersion(String friendlyName, int minimum)
	{
		_friendlyName = friendlyName;
		_minimum = minimum;
	}

	public String friendlyName()
	{
		return _friendlyName;
	}
	
	public boolean atOrAbove(MinecraftVersion other)
	{
		Preconditions.checkNotNull(other);
		
		return ordinal() <= other.ordinal();
	}

	public static MinecraftVersion fromInt(int version)
	{
		MinecraftVersion v = null;
		for (MinecraftVersion test : values())
		{
			if (version >= test._minimum)
			{
				v = test;
				break;
			}
		}
		
		return v;
	}
}