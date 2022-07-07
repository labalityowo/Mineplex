package mineplex.core.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;

public class UtilScheduler
{
	public static BukkitTask runEvery(UpdateType speed, Runnable action)
	{
		Plugin plugin = UtilServer.getPlugin();
		return plugin.getServer().getScheduler().runTaskTimer(plugin, action, 0, (int) Math.ceil(speed.getMilliseconds() / 50.0));
	}

	public static BukkitTask runAsyncEvery(UpdateType speed, Runnable action)
	{
		Plugin plugin = UtilServer.getPlugin();
		return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, action, 0, (int) Math.ceil(speed.getMilliseconds() / 50.0));
	}
}
