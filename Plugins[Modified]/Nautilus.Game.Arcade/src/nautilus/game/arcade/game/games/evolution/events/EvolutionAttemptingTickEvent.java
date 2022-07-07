package nautilus.game.arcade.game.games.evolution.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class EvolutionAttemptingTickEvent extends PlayerEvent
{
	/**
	 * @author Mysticate
	 */
	
	private static HandlerList _handlers = new HandlerList();
	
	private static HandlerList getHandlerList()
	{
		return _handlers;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
	
	private float _progress;

	public EvolutionAttemptingTickEvent(Player who, float progress)
	{
		super(who);
		
		_progress = progress;
	}
	
	public float getProgress()
	{
		return _progress;
	}

	public void setProgress(float progress)
	{
		_progress = progress;
	}
}
