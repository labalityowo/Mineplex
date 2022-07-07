package mineplex.game.clans.clans.siege.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.siege.weapon.SiegeWeapon;
import mineplex.game.clans.clans.siege.weapon.projectile.WeaponProjectile;

public class SiegeWeaponExplodeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private SiegeWeapon _weapon;
	private WeaponProjectile _projectile;
	
	private boolean _cancelled;
	
	public SiegeWeaponExplodeEvent(SiegeWeapon weapon, WeaponProjectile projectile)
	{
		_weapon = weapon;
		_projectile = projectile;
	}
	
	public SiegeWeapon getWeapon()
	{
		return _weapon;
	}
	
	public WeaponProjectile getProjectile()
	{
		return _projectile;
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