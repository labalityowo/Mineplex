package mineplex.gemhunters.world;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class TimeCycle implements Listener
{
	private static final int TICKS_DAY = 1;
	private static final int TICKS_NIGHT = 2;

	private final WorldDataModule _worldData;
	private World _world;

	private boolean _night;

	public TimeCycle(JavaPlugin plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		_worldData = Managers.get(WorldDataModule.class);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (_world == null)
		{
			_world = _worldData.World;
			return;
		}

		if (!_night && _world.getTime() > 12000)
		{
			_night = true;
		}

		if (_world.getTime() >= 23900)
		{
			_world.setTime(0);
			_night = false;
		}

		_world.setTime(_world.getTime() + (isNight() ? TICKS_NIGHT : TICKS_DAY));
	}

	public boolean isNight()
	{
		return _world.getTime() > 12000;
	}
}