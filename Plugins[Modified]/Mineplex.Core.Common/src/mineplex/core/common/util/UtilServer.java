package mineplex.core.common.util;

import com.google.common.collect.Lists;

import mineplex.core.common.Constants;
import mineplex.core.common.events.PlayerRecieveBroadcastEvent;
import mineplex.serverdata.Region;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class UtilServer
{
	private static boolean TEST_OVERRIDE = false;

	static {
		TEST_OVERRIDE = new File("TEST_OVERRIDE.dat").exists();
	}

	public static Player[] getPlayers()
	{
		return getServer().getOnlinePlayers().toArray(new Player[0]);
	}

	public static Collection<? extends Player> getPlayersCollection()
	{
		return getServer().getOnlinePlayers();
	}

	public static List<Player> getSortedPlayers()
	{
		return getSortedPlayers(Comparator.comparing(HumanEntity::getName));
	}

	public static List<Player> getSortedPlayers(Comparator<Player> comparator)
	{
		ArrayList<Player> players = new ArrayList<Player>(getServer().getOnlinePlayers());
		Collections.sort(players, comparator);
		return players;
	}

	public static Server getServer()
	{
		return Bukkit.getServer();
	}

	public static void broadcast(String message)
	{
		for (Player cur : getPlayers())
		{
			if (!UtilServer.CallEvent(new PlayerRecieveBroadcastEvent(cur, message)).isCancelled())
				UtilPlayer.message(cur, message);
		}
	}

	public static void broadcast(LinkedList<String> messages)
	{
		for (Player cur : getPlayers())
			UtilPlayer.message(cur, messages);
	}

	public static void broadcastSpecial(String event, String message)
	{
		for (Player cur : getPlayers())
		{
			UtilPlayer.message(cur, "§b§l" + event);
			UtilPlayer.message(cur, message);
			cur.playSound(cur.getLocation(), Sound.ORB_PICKUP, 2f, 0f);
			cur.playSound(cur.getLocation(), Sound.ORB_PICKUP, 2f, 0f);
		}
	}

	public static void broadcast(String sender, String message)
	{
		broadcast("§f§l" + sender + " " + "§b" + message);
	}

	public static void broadcastMagic(String sender, String message)
	{
		broadcast("§2§k" + message);
	}

	public static double getFilledPercent()
	{
		return (double) getPlayers().length / (double) UtilServer.getServer().getMaxPlayers();
	}

	public static void RegisterEvents(Listener listener)
	{
		getPluginManager().registerEvents(listener, getPlugin());
	}

	public static void Unregister(Listener listener)
	{
		HandlerList.unregisterAll(listener);
	}

	public static JavaPlugin getPlugin()
	{
		return JavaPlugin.getProvidingPlugin(UtilServer.class);
	}

	public static PluginManager getPluginManager()
	{
		return getServer().getPluginManager();
	}

	public static <T extends Event> T CallEvent(T event)
	{
		getPluginManager().callEvent(event);
		return event;
	}

	public static void repeat(BukkitRunnable runnable, long time)
	{
		runnable.runTaskTimer(getPlugin(), time, time);
	}

	public static Collection<Player> GetPlayers()
	{
		return Lists.newArrayList(getPlayers());
	}

	public static String getServerName()
	{
		return getPlugin().getConfig().getString("serverstatus.name");
	}

	public static Region getRegion()
	{
		return getPlugin().getConfig().getBoolean("serverstatus.us") ? Region.US : Region.EU;
	}

	public static String getGroup()
	{
		return getPlugin().getConfig().getString("serverstatus.group");
	}

	public static boolean isTestServer()
	{
		return isTestServer(true);
	}

	public static boolean isTestServer(boolean bypass)
	{
		return getPlugin().getConfig().getString("serverstatus.group").equalsIgnoreCase("Testing") || (bypass && TEST_OVERRIDE);
	}

	public static boolean isDevServer()
	{
		return !isTestServer() && isDevServer(getServerName());
	}

	public static boolean isDevServer(String name)
	{
		try
		{
			int index = name.lastIndexOf('-');
			if (index != -1)
			{
				int id = Integer.parseInt(name.substring(index + 1));
				return id >= 777;
			}
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
		return false;
	}

	public static boolean isHubServer()
	{
		return getPlugin().getConfig().getString("serverstatus.group").equalsIgnoreCase("Lobby");
	}

	public static void raiseError(RuntimeException throwable)
	{
		if (isTestServer())
		{
			throw throwable;
		}
		else
		{
			System.out.println("ERROR WAS RAISED");
			throwable.printStackTrace(System.out);
		}
	}

	public static BukkitTask runAsync(Runnable runnable)
	{
		return getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), runnable);
	}

	public static BukkitTask runAsync(Runnable runnable, long time)
	{
		return getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(getPlugin(), runnable, time);
	}

	public static BukkitTask runAsyncTimer(Runnable runnable, long time, long period)
	{
		return getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), runnable, time, period);
	}

	public static BukkitTask runSync(Runnable runnable)
	{
		return getPlugin().getServer().getScheduler().runTask(getPlugin(), runnable);
	}

	public static BukkitTask runSyncLater(Runnable runnable, long delay)
	{
		return getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), runnable, delay);
	}

	public static BukkitTask runSyncTimer(Runnable runnable, long delay, long period)
	{
		return getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), runnable, delay, period);
	}

	public static BukkitTask runSyncTimer(BukkitRunnable runnable, long delay, long period)
	{
		return runnable.runTaskTimer(getPlugin(), delay, period);
	}
}
