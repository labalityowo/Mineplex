package mineplex.mapparser;

import org.bukkit.plugin.java.JavaPlugin;

public class Ticker implements Runnable 
{
	private JavaPlugin _plugin;
	
	public Ticker(JavaPlugin plugin)
	{
		_plugin = plugin;
		_plugin.getServer().getScheduler().scheduleSyncRepeatingTask(_plugin, this, 0L, 1L);
	}
	
	@Override
	public void run() 
	{
		_plugin.getServer().getPluginManager().callEvent(new TickEvent());
	}
}
