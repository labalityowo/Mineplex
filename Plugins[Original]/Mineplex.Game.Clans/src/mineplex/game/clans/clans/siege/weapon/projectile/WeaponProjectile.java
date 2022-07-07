package mineplex.game.clans.clans.siege.weapon.projectile;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.siege.events.SiegeWeaponExplodeEvent;
import mineplex.game.clans.clans.siege.weapon.SiegeWeapon;

public abstract class WeaponProjectile implements Listener
{
	protected Location _origin;
	protected Entity _projectileEntity;
	
	protected SiegeWeapon _weapon;
	
	protected double _yawRot;
	protected double _xMulti;
	protected double _yVel;
	
	protected boolean _dead;
	
	protected Player _shooter;
	
	public WeaponProjectile(SiegeWeapon weapon, Location origin, double yawRot, double yVel, double xMulti)
	{
		_shooter = weapon.getRider();
		_weapon = weapon;
		_origin = origin;
		_yawRot = yawRot;
		_yVel = yVel;
		_xMulti = xMulti;
		
		UtilServer.getPluginManager().registerEvents(this, weapon.getClans().getPlugin());
		
		_projectileEntity = spawn();
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if (_projectileEntity == null || _projectileEntity.isDead())
		{
			die();
			return;
		}
		
		if (_projectileEntity.getTicksLived() <= 10)
		{
			return;
		}
		
		boolean moving = Math.abs(_projectileEntity.getVelocity().getX()) > 0.01 || Math.abs(_projectileEntity.getVelocity().getZ()) > 0.01;
		
		// Some rough collision detection. Not perfect, but the best I could conjure up myself.
		if ((!moving && !UtilBlock.boundless(_projectileEntity.getLocation(), 2)) || (_projectileEntity instanceof TNTPrimed && _projectileEntity.getTicksLived() >= 80))
		{
			SiegeWeaponExplodeEvent newEvent = UtilServer.CallEvent(new SiegeWeaponExplodeEvent(_weapon, this));
			
			if (!newEvent.isCancelled())
			{
				new Crater(_weapon, this, _projectileEntity.getLocation());
				UtilServer.getServer().getOnlinePlayers().forEach(player -> player.playSound(_projectileEntity.getLocation(), Sound.EXPLODE, 1.f, 1.f));
			}
			
			die();
			return;
		}
		
		if (_projectileEntity.getTicksLived() > (15 * 20))
		{
			die();
		}
	}
	
	public boolean hasDied()
	{
		return _dead;
	}
	
	public Location getLocation()
	{
		return _projectileEntity.getLocation();
	}
	
	public void setLocation(Location location)
	{
		_projectileEntity.teleport(location);
	}
	
	private void die()
	{
		HandlerList.unregisterAll(this);
		
		if (_projectileEntity != null)
		{
			_projectileEntity.remove();
			_projectileEntity = null;
		}
		
		_dead = true;
	}
	
	public abstract Entity spawn();

	public Player getShooter()
	{
		return _shooter;
	}
}
