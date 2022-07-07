package mineplex.core.updater;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.updater.event.UpdateEvent;

import org.bukkit.plugin.java.JavaPlugin;

@ReflectivelyCreateMiniPlugin
public class Updater extends MiniPlugin implements Runnable
{
	private Updater()
	{
		super("Updater Task");

		_plugin.getServer().getScheduler().scheduleSyncRepeatingTask(_plugin, this, 0L, 1L);
	}
	
	@Override
	public void run() 
	{
		for (UpdateType updateType : UpdateType.values())
		{
			if (updateType.Elapsed())
			{
				_plugin.getServer().getPluginManager().callEvent(new UpdateEvent(updateType));
			}
		}
	}
}
