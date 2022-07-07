package mineplex.core.stats.event;


import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class StatChangeEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();

	private String _statName;
	private long _valueBefore;
	private long _valueAfter;

	public StatChangeEvent(Player player, String statName, long valueBefore, long valueAfter)
	{
		super(player);
		_statName = statName;
		_valueBefore = valueBefore;
		_valueAfter = valueAfter;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public String getStatName()
	{
		return _statName;
	}

	public long getValueBefore()
	{
		return _valueBefore;
	}

	public long getValueAfter()
	{
		return _valueAfter;
	}
}
