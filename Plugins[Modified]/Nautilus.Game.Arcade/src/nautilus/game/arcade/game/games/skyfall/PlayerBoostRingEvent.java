package nautilus.game.arcade.game.games.skyfall;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Event which is triggered by Players flying through {@link BoosterRing}.
 *
 * @author xXVevzZXx
 */
public class PlayerBoostRingEvent extends PlayerEvent implements Cancellable
{

	private boolean _cancelled;
	
	private double _baseStrength;
	private double _strength;
	
	private BoosterRing _ring;
	
	public PlayerBoostRingEvent(Player player, float strength, BoosterRing ring)
	{
		super(player);
		
		_baseStrength = strength;
		_strength = strength;
		_ring = ring;
	}
	
	public void setStrength(double strenght)
	{
		_strength = strenght;
	}
	
	public void increaseStrength(double strenght)
	{
		_strength += strenght;
	}
	
	public void decreaseStrength(double strenght)
	{
		_strength -= strenght;
	}
	
	public void multiplyStrength(double multy)
	{
		_strength *= multy;
	}
	
	public double getStrength()
	{
		return _strength;
	}
	
	public double getBaseStrength()
	{
		return _baseStrength;
	}
	
	public BoosterRing getRing()
	{
		return _ring;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel; 
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	private static HandlerList _handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}
}
