package mineplex.game.clans.clans.siege.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.siege.weapon.SiegeWeapon;

public class MountSiegeWeaponEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private SiegeWeapon _weapon;
	
	private boolean _cancelled;
	
	public MountSiegeWeaponEvent(Player player, SiegeWeapon weapon)
	{
		_player = player;
		_weapon = weapon;
	}
	
	public SiegeWeapon getWeapon()
	{
		return _weapon;
	}
	
	public Player getPlayer()
	{
		return _player;
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