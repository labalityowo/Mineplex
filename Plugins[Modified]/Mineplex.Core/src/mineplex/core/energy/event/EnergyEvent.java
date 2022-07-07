package mineplex.core.energy.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EnergyEvent extends Event implements Cancellable
{
	public enum EnergyChangeReason 
	{
		Recharge,
		Use
	}

	private static final HandlerList handlers = new HandlerList();
	private boolean _cancelled = false;

	private Player _player;
	private double _amount;
	private double _mods;
	private EnergyChangeReason _reason;

	public EnergyEvent(Player player, double amount, EnergyChangeReason reason)
	{
		_player = player;
		_amount = amount;
		_reason = reason;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}

	public Player GetPlayer()
	{
		return _player;
	}
	
	public double GetAmount()
	{
		return _amount;
	}
	
	public void AddMod(double mod)
	{
		_mods += mod;
	}
	
	public double GetTotalAmount()
	{
		return _amount + _mods;
	}
	
	public EnergyChangeReason GetReason()
	{
		return _reason;
	}
}
