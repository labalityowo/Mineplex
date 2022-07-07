package nautilus.game.arcade.game.games.milkcow;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class MilkRemoveEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private int _milkToRemove;

	public MilkRemoveEvent(Player who, int milkToRemove)
	{
		super(who);

		_milkToRemove = milkToRemove;
	}

	public HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }

	public void setMilkToRemove(int milkToRemove)
	{
		_milkToRemove = milkToRemove;
	}

	public int getMilkToRemove()
	{
		return _milkToRemove;
	}
}
