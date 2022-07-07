package nautilus.game.arcade.game.games.evolution.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class EvolutionPostEvolveEvent extends PlayerEvent
{
	/**
	 * @author Mysticate
	 */
	
	private static HandlerList _handlers = new HandlerList();

	public EvolutionPostEvolveEvent(Player who)
	{
		super(who);
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
