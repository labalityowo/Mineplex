package mineplex.core.treasure.event;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

import mineplex.core.treasure.TreasureSession;
import mineplex.core.treasure.types.TreasureType;

public abstract class TreasureEvent extends PlayerEvent
{

	private final TreasureSession _session;

	public TreasureEvent(Player who, TreasureSession session)
	{
		super(who);

		_session = session;
	}

	public TreasureSession getSession()
	{
		return _session;
	}

	public TreasureType getTreasureType()
	{
		return _session.getTreasure().getTreasureType();
	}
}
