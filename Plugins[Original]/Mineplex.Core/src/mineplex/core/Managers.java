package mineplex.core;

import mineplex.core.common.util.UtilServer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class will manage all the MiniPlugin instances. It's not the best way to do it, but it works given how
 * we use MiniPlugins right now, so let's not fret about that
 */
public class Managers
{
	private static final Object LOCK = new Object();

	private static final Map<Class<?>, Object> MANAGER_MAP = new HashMap<>();

	private static final LinkedList<Class<?>> INSTANTIATING = new LinkedList<>();

	/**
	 * Gets a Manager ({@link MiniPlugin}) based on its class
	 *
	 * @param clazz The class of the MiniPlugin to return
	 * @return The mapped MiniPlugin, or null if not found
	 */
	public static <T extends MiniPlugin> T get(Class<T> clazz)
	{
		Object result;

		synchronized (LOCK)
		{
			result = MANAGER_MAP.get(clazz);
		}

		return clazz.cast(result);
	}

	/**
	 * Gets the given module, and initializes if necessary
	 * @param clazz
	 * @return
	 */
	public static <T extends MiniPlugin> T require(Class<T> clazz)
	{
		Object result = null;

		synchronized (LOCK)
		{
			if (!MANAGER_MAP.containsKey(clazz))
			{
				if (INSTANTIATING.contains(clazz))
				{
					List<Class<?>> all = new ArrayList<>(INSTANTIATING);
					all.add(clazz);
					throw new IllegalStateException("Circular instantiation: " + all);
				}

				INSTANTIATING.add(clazz);

				try
				{
					ReflectivelyCreateMiniPlugin annotation = clazz.getAnnotation(ReflectivelyCreateMiniPlugin.class);
					if (annotation != null)
					{
						Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
						defaultConstructor.setAccessible(true);
						result = defaultConstructor.newInstance();
					}
				}
				catch (ReflectiveOperationException ex)
				{
					if (!UtilServer.isTestServer())
					{
						System.out.println("============== WARNING ==============");
						System.out.println(" ");
						System.out.println(" ");
						System.out.println(" ");
						System.out.println("Failed to reflectively create MiniPlugin. How did this happen?");
						ex.printStackTrace(System.out);
						System.out.println(" ");
						System.out.println(" ");
						System.out.println(" ");
						System.out.println("============== WARNING ==============");
					}
					else
					{
						throw new RuntimeException("Failed to reflectively create MiniPlugin", ex);
					}
				}

				if (INSTANTIATING.pollLast() != clazz)
				{
					throw new IllegalArgumentException("Wot");
				}
			}
			else
			{
				result = MANAGER_MAP.get(clazz);
			}
		}

		return clazz.cast(result);
	}

	public static void put(MiniPlugin manager)
	{
		if (manager == null) throw new NullPointerException("Manager cannot be null");

		synchronized (LOCK)
		{
			if (MANAGER_MAP.containsKey(manager.getClass()))
			{
				if (!UtilServer.isTestServer())
				{
					System.out.println("============== WARNING ==============");
					System.out.println(" ");
					System.out.println(" ");
					System.out.println(" ");
					System.out.println("Attempted to register " + manager.getClass().getName() + ", but it was already registered");
					new Exception("Stack trace").printStackTrace(System.out);
					System.out.println(" ");
					System.out.println(" ");
					System.out.println(" ");
					System.out.println("============== WARNING ==============");
				}
				else
				{
					throw new IllegalArgumentException("Manager " + manager.getClass().getName() + " is already registered");
				}
			}
			MANAGER_MAP.put(manager.getClass(), manager);
		}
	}

	public static void put(MiniPlugin manager, Class<? extends MiniPlugin> type)
	{
		if (manager == null) throw new NullPointerException("Manager cannot be null");
		if (!type.isAssignableFrom(manager.getClass())) throw new IllegalArgumentException(manager.getClass().getName() + " is not a subclass of " + type.getName());

		synchronized (LOCK)
		{
			if (MANAGER_MAP.containsKey(type))
			{
				if (!UtilServer.isTestServer())
				{
					System.out.println("============== WARNING ==============");
					System.out.println(" ");
					System.out.println(" ");
					System.out.println(" ");
					System.out.println("Attempted to register " + type.getName() + ", but it was already registered");
					new Exception("Stack trace").printStackTrace(System.out);
					System.out.println(" ");
					System.out.println(" ");
					System.out.println(" ");
					System.out.println("============== WARNING ==============");
				}
				else
				{
					throw new IllegalArgumentException("Manager " + type.getName() + " is already registered");
				}
			}
			MANAGER_MAP.put(type, manager);
		}
	}
}
