package nautilus.game.arcade.game.games.minestrike;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerHeadshotEvent extends PlayerEvent
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

	private final Player _shooter;

	public PlayerHeadshotEvent(Player who, Player shooter)
	{
		super(who);

		_shooter = shooter;
	}

	public Player getShooter()
	{
		return _shooter;
	}
}