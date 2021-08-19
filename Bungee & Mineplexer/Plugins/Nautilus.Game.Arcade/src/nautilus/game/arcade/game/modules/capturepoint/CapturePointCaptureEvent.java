package nautilus.game.arcade.game.modules.capturepoint;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CapturePointCaptureEvent extends Event
{

	private static final HandlerList _handlers = new HandlerList();

	private final CapturePoint _point;

	CapturePointCaptureEvent(CapturePoint point)
	{
		_point = point;
	}

	public CapturePoint getPoint()
	{
		return _point;
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
