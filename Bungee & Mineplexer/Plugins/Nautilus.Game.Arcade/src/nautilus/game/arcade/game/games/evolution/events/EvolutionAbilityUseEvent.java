package nautilus.game.arcade.game.games.evolution.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class EvolutionAbilityUseEvent extends PlayerEvent implements Cancellable
{
	/**
	 * @author Mysticate
	 */
	
	private static HandlerList _handlers = new HandlerList();
	
	private boolean _cancelled = false;
	
	private String _ability;
	private long _cooldown;
	
	public EvolutionAbilityUseEvent(Player who, String ability, long cooldown)
	{
		super(who);
		
		_ability = ability;
		_cooldown = cooldown;
	}
	
	public String getAbility()
	{
		return _ability;
	}
	
	public long getCooldown()
	{
		return _cooldown;
	}
	
	public void setCooldown(long cooldown)
	{
		_cooldown = cooldown;
	}
	
	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
}
