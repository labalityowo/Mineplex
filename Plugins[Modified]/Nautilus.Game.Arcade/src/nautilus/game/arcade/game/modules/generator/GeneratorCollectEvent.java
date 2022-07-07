package nautilus.game.arcade.game.modules.generator;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class GeneratorCollectEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final Generator _generator;

	public GeneratorCollectEvent(Player who, Generator generator)
	{
		super(who);

		_generator = generator;
	}

	public Generator getGenerator()
	{
		return _generator;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
