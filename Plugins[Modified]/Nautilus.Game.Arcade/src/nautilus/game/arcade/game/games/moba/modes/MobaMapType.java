package nautilus.game.arcade.game.games.moba.modes;

import nautilus.game.arcade.game.games.moba.Moba;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import java.lang.reflect.InvocationTargetException;

public enum MobaMapType
{

	HEROES_VALLEY("Heroes Valley", MobaHeroesValleyMap.class),
	MONOCHROME("Monochrome", MobaMonochromeMap.class)

	;

	private final String _name;
	private final Class<? extends MobaMap> _clazz;

	MobaMapType(String name, Class<? extends MobaMap> clazz)
	{
		_name = name;
		_clazz = clazz;
	}

	public String getName()
	{
		return _name;
	}

	public MobaMap createInstance(Moba host)
	{
		try
		{
			return _clazz.getConstructor(Moba.class).newInstance(host);
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
