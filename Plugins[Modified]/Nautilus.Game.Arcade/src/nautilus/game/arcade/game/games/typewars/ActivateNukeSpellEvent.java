package nautilus.game.arcade.game.games.typewars;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ActivateNukeSpellEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	private Player _player;
	private ArrayList<Minion> _minions;

	public ActivateNukeSpellEvent(Player player, ArrayList<Minion> minions)
	{
		_player = player;
		_minions = minions;
	}

	public Player getPlayer()
	{
		return _player;
	}
	
	public ArrayList<Minion> getMinions()
	{
		return _minions;
	}
}
