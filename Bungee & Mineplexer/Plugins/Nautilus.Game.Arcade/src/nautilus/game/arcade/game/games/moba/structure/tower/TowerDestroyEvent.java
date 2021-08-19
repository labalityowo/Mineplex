package nautilus.game.arcade.game.games.moba.structure.tower;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TowerDestroyEvent extends Event
{

	private static final HandlerList _handlers = new HandlerList();

	private Tower _tower;

	public TowerDestroyEvent(Tower tower)
	{
		_tower = tower;
	}

	public Tower getTower()
	{
		return _tower;
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
