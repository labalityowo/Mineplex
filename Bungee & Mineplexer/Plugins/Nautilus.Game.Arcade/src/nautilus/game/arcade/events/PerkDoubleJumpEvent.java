package nautilus.game.arcade.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PerkDoubleJumpEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();

	private double _power;
	private double _heightMax;
	private boolean _control, _cancelled;

	public PerkDoubleJumpEvent(Player who, double power, double heightMax, boolean control)
	{
		super(who);

		_power = power;
		_heightMax = heightMax;
		_control = control;
	}

	public void setPower(double power)
	{
		_power = power;
	}

	public double getPower()
	{
		return _power;
	}

	public void setHeightMax(double heightMax)
	{
		_heightMax = heightMax;
	}

	public double getHeightMax()
	{
		return _heightMax;
	}

	public void setControl(boolean control)
	{
		_control = control;
	}

	public boolean isControlled()
	{
		return _control;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean b)
	{
		_cancelled = b;
	}

	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}
}