package nautilus.game.pvp.worldevent;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import net.minecraft.server.v1_6_R3.EntityCreature;
import net.minecraft.server.v1_6_R3.Navigation;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent;

public abstract class EventMobMinion extends EventMob 
{
	protected EventMobBoss _host;
	protected double _radialLead = 0;
	
	protected LivingEntity _target = null;
	
	protected int _distReturnSoft = 12;
	protected int _distReturnHard = 24;
	protected int _distRemove = 32;
	
	protected int _distTarget = 16;
	
	public EventMobMinion(EventBase event, Location location, String name, boolean useName, int health, EntityType type, EventMobBoss host) 
	{
		super(event, location, name, useName, health, type);
		
		_host = host;
		_host.MinionRegister(this);
	}
	
	public LivingEntity GetTarget() 
	{
		return _target;
	}
	
	public void SetTarget(LivingEntity ent) 
	{
		_target = ent;
	}
	
	@EventHandler
	public void Target(UpdateEvent event)
	{ 
		if (event.getType() != UpdateType.SEC)
			return;

		if (_target != null && (_target.isDead() || !_target.isValid()))
		{
			_target = null;
		}
			

		//Hard Return
		if (UtilMath.offset(_host.GetEntity(), GetEntity()) > _distReturnHard)
		{
			_target = _host.GetEntity();
		}
			

		//Soft Return
		if (_target == null && UtilMath.offset(_host.GetEntity(), GetEntity()) > _distReturnSoft)
		{
			_target = _host.GetEntity();
		}
			

		//Cancel Return
		if (_target != null && _target.equals(_host.GetEntity()) && UtilMath.offset(_host.GetEntity(), GetEntity()) < _distReturnSoft)
		{
			_target = null;
		}
			

		//Target Player
		if (_target == null)
		{

			for (Player cur : UtilPlayer.getNearby(GetEntity().getLocation(), _distTarget))
			{
				if (!_host.MinionTarget(cur))
					continue;
				
				_target = cur;
			}
		}

		if (_target != null)
		{
			((Creature)GetEntity()).setTarget(_target);
		}
		else
		{
			((Creature)GetEntity()).setTarget(null);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void TargetCancel(EntityTargetEvent event)
	{	
		if (GetEntity() == null)
			return;
		
		if (event.getTarget() == null)
			return;
		
		if (!event.getEntity().equals(GetEntity()))
			return;
		
		if (TargetCancelCustom(event.getTarget()))
			event.setCancelled(true);
			
		if (_target == null || _target.isDead() || !_target.isValid() || !event.getTarget().equals(_target))
			event.setCancelled(true);
	}
	
	public boolean TargetCancelCustom(Entity entity) 
	{
		return false;
	}

	@EventHandler
	public void Despawn(UpdateEvent event)
	{		
		if (event.getType() != UpdateType.SEC)
			return;

		if (	GetEntity() == null || !GetEntity().isValid() || 
				_host.GetEntity() == null || _host.GetEntity().isDead() || !_host.GetEntity().isValid() ||
				UtilMath.offset(GetEntity(), _host.GetEntity()) > _distRemove)
		{
			Remove();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void Return(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!Valid())
			return;
		
		if (_target == null || _target.isDead() || !_target.isValid() || !_target.equals(_host.GetEntity()))
			return;

		//Move
		EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
		Navigation nav = ec.getNavigation();
		Location loc = _host.GetEntity().getLocation();
		nav.a(loc.getX(), loc.getY(), loc.getZ(), 0.4f);
	}
	
	public boolean Valid() 
	{
		if (GetEntity() == null || !GetEntity().isValid() ||  _host == null ||
				_host.GetEntity() == null || _host.GetEntity().isDead() || !_host.GetEntity().isValid())
		{
			Die();
			
			return false;
		}
		
		return true;
	}

	@Override
	public void Remove()
	{
		_host.MinionDeregister(this);
		
		if (GetEntity() != null)
			GetEntity().remove();

		Event.CreatureDeregister(this);	
	}
	
	@Override
	public void Loot() 
	{

	}
	
	public void SetRadialLead(double lead)
	{
		_radialLead = lead;
	}
	
	public abstract void StateChange(int newState);

	
}
