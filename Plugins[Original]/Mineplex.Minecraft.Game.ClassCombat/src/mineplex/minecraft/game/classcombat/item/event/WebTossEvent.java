package mineplex.minecraft.game.classcombat.item.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WebTossEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }
	
	private boolean _cancelled;
	public boolean isCancelled() { return _cancelled; }
	public void setCancelled(boolean cancelled) { _cancelled = cancelled; }
	
	private Location _location;
	public Location getLocation() { return _location; }
	
	private LivingEntity _thrower;
	public LivingEntity getThrower() { return _thrower; }

    public WebTossEvent(LivingEntity thrower, Location location)
    {
    	_location = location;
    	_thrower = thrower;
    }
}
