package mineplex.game.clans.clans.siege.weapon.projectile.event;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.siege.weapon.SiegeWeapon;
import mineplex.game.clans.clans.siege.weapon.projectile.Crater;
import mineplex.game.clans.clans.siege.weapon.projectile.WeaponProjectile;

public class PreCraterSetBlockEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private SiegeWeapon _weapon;
	private WeaponProjectile _projectile;
	private Set<Block> _blocks;
	private Crater _crater;
	
	
	private boolean _cancelled;
	
	public PreCraterSetBlockEvent(Set<Block> blocks, Crater crater, SiegeWeapon weapon, WeaponProjectile projectile)
	{
		_blocks = blocks;
		_crater = crater;
		_weapon = weapon;
		_projectile = projectile;
	}
	
	public Set<Block> getBlocks()
	{
		return _blocks;
	}
	
	public Crater getCrater()
	{
		return _crater;
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