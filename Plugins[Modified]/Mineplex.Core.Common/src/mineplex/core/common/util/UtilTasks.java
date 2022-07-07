package mineplex.core.common.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.function.Consumer;

public class UtilTasks
{
	private static final JavaPlugin LOADING_PLUGIN = JavaPlugin.getProvidingPlugin(UtilTasks.class);

	private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

	public static Runnable onMainThread(Runnable original)
	{
		return () ->
		{
			if (Bukkit.isPrimaryThread())
			{
				original.run();
			}
			else
			{
				SCHEDULER.runTask(LOADING_PLUGIN, original);
			}
		};
	}

	public static <T> Consumer<T> onMainThread(Consumer<T> original)
	{
		return t ->
		{
			if (original != null)
			{
				onMainThread(() ->
				{
					original.accept(t);
				}).run();
			}
		};
	}
}
