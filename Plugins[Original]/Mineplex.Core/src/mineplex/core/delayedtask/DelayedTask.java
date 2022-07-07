package mineplex.core.delayedtask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniClientPlugin;
import mineplex.core.common.util.Callback;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import java.util.UUID;

public class DelayedTask extends MiniClientPlugin<DelayedTaskClient>
{
	public static DelayedTask Instance;
	
	public DelayedTask(JavaPlugin plugin)
	{
		super("Delayed Task", plugin);
	}
	
	public static void Initialize(JavaPlugin plugin)
	{
		Instance = new DelayedTask(plugin);
	}
	
	public void doDelay(Player player, String task, Callback<DelayedTaskClient> end, Callback<DelayedTaskClient> tick, Callback<DelayedTaskClient> cancel, long wait, boolean allowMovement)
	{
		Get(player).insert(new Task(Get(player), task, end, tick, cancel, wait, allowMovement));
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			Get(player).tick();
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Get(event.getPlayer()).cleanup();
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event)
	{
		Get(event.getPlayer()).cleanup();
	}
	
	@Override
	protected DelayedTaskClient addPlayer(UUID uuid)
	{
		return new DelayedTaskClient(Bukkit.getPlayer(uuid));
	}

	public boolean HasTask(Player player, String task)
	{
		return Get(player).getStartTime(task) != -1;
	}
	
	public boolean HasTask(String player, String task)
	{
		return HasTask(Bukkit.getPlayer(player), task);
	}
}
