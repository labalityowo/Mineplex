package nautilus.game.arcade.game.games.typewars;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ActivateSpellEvent extends Event
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
	private Spell _spell;

	public ActivateSpellEvent(Player player, Spell spell)
	{
		_player = player;
		_spell = spell;
	}

	public Player getPlayer()
	{
		return _player;
	}
	
	public Spell getSpell()
	{
		return _spell;
	}
}
