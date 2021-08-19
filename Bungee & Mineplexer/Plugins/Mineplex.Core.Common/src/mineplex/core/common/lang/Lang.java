package mineplex.core.common.lang;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import mineplex.core.common.util.F;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class Lang
{
	public static interface PlayerLocaleFunction
	{
		public Locale getLocaleOfPlayer(Player player);
	}


	private static PlayerLocaleFunction _playerLocaleFunction = null;
	private static final Map<Locale, ResourceBundle> _localeResourceBundles = Collections.synchronizedMap(new HashMap<Locale, ResourceBundle>());
	

	public Lang()
	{
		init();
	}
 
	 
	private void init()
	{
		System.out.println(F.main("i18n","Attempting to initialize resource bundles..."));
		
		try
		{
			// Locales over which we should iterate and load.
			for (Locale loc : new Locale[] {
					Locale.ENGLISH,
					Locale.GERMAN
			})
			{
				ResourceBundle bundle = ResourceBundle.getBundle("mineplex.core.common.lang.MineplexBundle", loc);
				_localeResourceBundles.put(loc, bundle);
				System.out.println("Loaded " + loc.toString() + "...");
			}
		}
		catch (MissingResourceException e)
		{
			System.err.println("AN ERROR OCCURED WHILE ATTEMPTING TO LOAD RESOURCE LOCALES");
			// For now at least, crash the runtime.
			throw new RuntimeException(e);
		}
	}
	
	public static PlayerLocaleFunction getPlayerLocaleFunction()
	{
		return _playerLocaleFunction;
	}

	public static void setPlayerLocaleFunction(PlayerLocaleFunction playerLocaleFunction)
	{
		_playerLocaleFunction = playerLocaleFunction;
	}

	public static Locale getPlayerLocale(Player player)
	{
		if (getPlayerLocaleFunction() == null)
			return Locale.getDefault();
		else
			return getPlayerLocaleFunction().getLocaleOfPlayer(player);
	}

	public static ResourceBundle getResourceBundle(Locale locale)
	{
		synchronized (_localeResourceBundles)
		{
			if (_localeResourceBundles.containsKey(locale))
				return _localeResourceBundles.get(locale);
			else
			{
				return _localeResourceBundles.get(Locale.ENGLISH);
			}
		}
	}

	public static ResourceBundle getBestResourceBundle(Locale locale)
	{
		ResourceBundle bundle = getResourceBundle(locale);

		if (bundle == null && !locale.equals(Locale.getDefault()))
			bundle = getResourceBundle(Locale.getDefault());

		if (bundle == null && !locale.equals(Locale.ENGLISH))
			bundle = getResourceBundle(Locale.ENGLISH);

		return bundle;
	}

	/**
	 * Shorthand method for obtaining and translating a key.
	 */
	public static String tr(String key, Entity entity, Object... args)
	{
		IntlString string = key(key);
		
		for (Object a : args)
			string.arg(a);
		
		return string.tr(entity);
	}
	
	public static String get(String key)
	{
		return get(key, (Locale) null);
	}

	public static String get(String key, Locale locale)
	{
		if (key == null)
			return null;
		else if (key.isEmpty())
			return "";
		else
		{
			if (locale == null)
				locale = Locale.getDefault();

			ResourceBundle bundle = getBestResourceBundle(locale);
			if (bundle == null)
				return null;

			return bundle.getString(key);
		}
	}

	public static String get(String key, Player player)
	{
		return get(key, getPlayerLocale(player));
	}
	
	public static IntlString key(String key, ChatColor... styles)
	{
		return new IntlString(key, styles);
	}

	public static IntlString key(String key, String style)
	{
		return new IntlString(key, style);
	}
}