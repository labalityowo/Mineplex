package mineplex.core.rankGiveaway.titangiveaway;

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

public class TitanGiveawayAnimation implements Listener
{	
	private Location _location;
	private Long _duration, _startTime, _worldTime;
	
	public TitanGiveawayAnimation(TitanGiveawayManager manager, Location start, Long duration)
	{
		_location = start.clone();
		_duration = duration;
		_startTime = System.currentTimeMillis();
//		_worldTime = start.getWorld().getTime();
		Bukkit.getPluginManager().registerEvents(this, manager.getPlugin());
	}
	
	public TitanGiveawayAnimation(TitanGiveawayManager manager, Location start)
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
		
//		_location.getWorld().setTime(UtilMath.random.nextLong());
		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(_location, Sound.ORB_PICKUP, 5, 5);
			UtilFirework.packetPlayFirework(player, _location, Type.BURST, Color.RED, true, false);
		}
	}
	
	private void remove()
	{
//		_location.getWorld().setTime(_worldTime);
		HandlerList.unregisterAll(this);
	}
}