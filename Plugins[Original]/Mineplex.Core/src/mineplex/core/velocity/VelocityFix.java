package mineplex.core.velocity;

import java.util.Iterator;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.VelocityReceiver;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class VelocityFix extends MiniPlugin implements VelocityReceiver
{
	/*
	 * The purpose of this class is to fix a bug inherent in Minecraft, 
	 * where player join order will somehow modify the velocity sent to players.
	 * 
	 * To fix it, we simply save the velocity that the player should have received,
	 * then we re-set those values in CB the moment its about to actually apply the velocity to a player.
	 * 
	 * The problem was caused by the fact that CB does not run a PlayerVelocityEvent the moment we
	 * set a players velocity, instead it waits until the next tick, and the velocity may have been changed.
	 * 
	 */
	
	private NautHashMap<Player, Vector> _velocityData = new NautHashMap<Player,Vector>();
	
	public VelocityFix(JavaPlugin plugin)
	{
		super("Velocity Fix", plugin);
		
		UtilAction.registerVelocityFix(this);
	}
	
	@Override
	public void setPlayerVelocity(Player player, Vector velocity) 
	{
		_velocityData.put(player, velocity);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void fixVelocity(PlayerVelocityEvent event)
	{
		if (_velocityData.containsKey(event.getPlayer()))
			event.getPlayer().setVelocity(_velocityData.remove(event.getPlayer()));
	}
	
	@EventHandler
	public void cleanVelocity(PlayerQuitEvent event)
	{
		_velocityData.remove(event.getPlayer());
	}
	
	@EventHandler
	public void cleanVelocity(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;
		
		Iterator<Player> keyIter = _velocityData.keySet().iterator();
		
		while (keyIter.hasNext())
		{
			Player player = keyIter.next();
			
			if (player.isOnline())
				keyIter.remove();
		}
	}
}
