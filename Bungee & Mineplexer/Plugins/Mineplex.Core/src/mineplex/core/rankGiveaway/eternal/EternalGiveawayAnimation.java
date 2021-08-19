package mineplex.core.rankGiveaway.eternal;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class EternalGiveawayAnimation implements Listener
{
	private Location _location;
	private Long _duration, _startTime, _worldTime;

	public EternalGiveawayAnimation(EternalGiveawayManager manager, Location start, Long duration)
	{
		_location = start.clone();
		_duration = duration;
		_startTime = System.currentTimeMillis();
		Bukkit.getPluginManager().registerEvents(this, manager.getPlugin());
	}

	public EternalGiveawayAnimation(EternalGiveawayManager manager, Location start)
	{
		this(manager, start, 11111L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void tick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (UtilTime.elapsed(_startTime, _duration))
		{
			remove();
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(_location, Sound.ORB_PICKUP, 5, 5);
			UtilFirework.packetPlayFirework(player, _location, Type.BURST, Color.fromRGB(255, 105, 180), true, false);
		}
	}

	private void remove()
	{
		HandlerList.unregisterAll(this);
	}
}