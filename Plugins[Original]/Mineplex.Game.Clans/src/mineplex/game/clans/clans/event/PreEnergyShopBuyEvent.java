package mineplex.game.clans.clans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PreEnergyShopBuyEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private boolean _cancelled;
	
	private Player _player;
	private int _energy;
	private int _cost;
	
	public PreEnergyShopBuyEvent(Player player, int energy, int cost)
	{
		_player = player;
		_energy = energy;
		_cost = cost;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public int getEnergy()
	{
		return _energy;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}