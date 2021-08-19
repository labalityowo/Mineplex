package nautilus.game.pvp.worldevent;

import java.util.HashMap;

import nautilus.game.pvp.worldevent.EventBase.EventState;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.world.ChunkUnloadEvent;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;

public abstract class EventMob implements Listener
{
	public EventBase Event;

	private LivingEntity _entity;

	private Location _loc;
	private EntityType _type;

	private String _name;
	private double _healthCur;
	private double _healthMax;

	private long _lastAttacked = 0;
	private long _lastAttacker = 0;

	protected long _attackerDelay = 400;
	protected long _attackedDelay = 400;

	protected boolean _useName = true;

	private HashMap<String, Double> _damagers = new HashMap<String, Double>();

	public EventMob(EventBase event, Location location, String name, boolean useName, int health, EntityType type)
	{
		Event = event;

		_loc = location;
		_type = type;

		_name = name;
		_useName = useName;

		_healthMax = health;
		_healthCur = health;

		Spawn();

		Event.Manager.Creature().AddEntityName(_entity, GetName());	
	}

	public void UpdateName()
	{
		if (!_useName)
			return;

		if (_entity instanceof CraftLivingEntity)
		{
			String healthString = (int)_healthCur + "/" + (int)_healthMax;
			double per = (double)_healthCur / (double)_healthMax;
			if (per > 0.5)	healthString = C.cGreen + healthString;
			if (per > 0.2)	healthString = C.cYellow + healthString;
			else			healthString = C.cRed + healthString;

			healthString = C.cWhite + "(" + healthString + C.cWhite + ")";

			CraftLivingEntity ent = (CraftLivingEntity)_entity;
			ent.setCustomName(_name + " " + healthString); 
			ent.setCustomNameVisible(true);
		}
	}

	public void Spawn()
	{ 
		Event.Manager.Creature().SetForce(true);
		LivingEntity ent = (LivingEntity) GetLocation().getWorld().spawnEntity(GetLocation(), GetType());
		Event.Manager.Creature().SetForce(false);

		ent.setRemoveWhenFarAway(false);

		SetEntity(ent);

		ent.setMaxHealth(100d);
		ent.setHealth(100d);

		SpawnCustom();

		UpdateName();
	}

	@EventHandler
	public void ChunkDespawn(ChunkUnloadEvent event)
	{
		if (event.getChunk().equals(GetEntity().getLocation().getChunk()))
			event.setCancelled(true);
	}

	public void SpawnCustom()
	{
		//Null
	}

	@EventHandler
	public void Refresh(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (Event.GetState() == EventState.END) 
			return;

		if (!_loc.getChunk().isLoaded())
			return;

		if (GetEntity() == null)
		{
			System.out.println("Respawning (" + GetName() + ") @ " + UtilWorld.locToStrClean(_loc) + " because NULL.");
			Spawn();
		}

		else if (GetEntity().isDead())
		{
			System.out.println("Ending (" + GetName() + ") @ " + UtilWorld.locToStrClean(_loc) + " because DEAD.");
			Die();
		}

		else if (!GetEntity().isValid())
		{
			GetEntity().remove();

			System.out.println("Respawning (" + GetName() + ") @ " + UtilWorld.locToStrClean(_loc) + " because INVALID.");
			Spawn();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void DamagerRate(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (event.GetDamagerEntity(false) == null)
			return;

		if (!event.GetDamagerEntity(false).equals(GetEntity()))
			return;

		if (!UtilTime.elapsed(_lastAttacker, _attackerDelay))
		{
			event.SetCancelled("Event Creature Damager Rate");
			return;
		}
		
		DamagerCustom(event);

		_lastAttacker = System.currentTimeMillis();
	}
	
	public void DamagerCustom(CustomDamageEvent event)
	{

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void DamagedRate(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK && event.GetCause() != DamageCause.PROJECTILE)
			return;

		if (GetEntity() == null)
			return;

		if (!event.GetDamageeEntity().equals(GetEntity()))
			return;

		if (!UtilTime.elapsed(_lastAttacked, _attackedDelay))
		{
			event.SetCancelled("Event Creature Damagee Rate");
			return;
		}

		_lastAttacked = System.currentTimeMillis();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void DamagedBorderlands(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (GetEntity() == null)
			return;

		if (!event.GetDamageeEntity().equals(GetEntity()))
			return;

		//Record Damage
		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)		return;
		
		if (!Event.Manager.Clans().CUtil().isBorderlands(damager.getLocation()) ||
			!Event.Manager.Clans().CUtil().isBorderlands(GetEntity().getLocation()))
		{
			event.SetCancelled("Not Borderlands");
			UtilPlayer.message(damager, F.main("World Event", "You cannot harm " + F.name(_name) + " outside of Borderlands."));
		}
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void Damaged(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (GetEntity() == null)
			return;

		if (!event.GetDamageeEntity().equals(GetEntity()))
			return;

		//Record Damage
		Player damager = event.GetDamagerPlayer(true);
		if (damager != null)	_damagers.put(damager.getName(), event.GetDamage());

		//Custom
		DamagedCustom(event);
		if (event.IsCancelled())
			return;

		double heal = ((CraftLivingEntity)event.GetDamageeEntity()).getMaxHealth();
		
		//Heal
		event.GetDamageeEntity().setHealth(heal);

		//Apply Damage
		ApplyDamage(event.GetDamage());

		//Record
		_lastAttacked = System.currentTimeMillis();
	}

	public void DamagedCustom(CustomDamageEvent event)
	{

	}

	public void ApplyDamage(double damage)
	{
		_healthCur -= damage;
		if (_healthCur <= 0)
			Die();

		//Update
		UpdateName();
	}

	@EventHandler
	public void Combust(EntityCombustEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getEntity().equals(GetEntity()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void DamageType(CustomDamageEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.GetCause() != DamageCause.SUFFOCATION && 
			event.GetCause() != DamageCause.DROWNING &&
			event.GetCause() != DamageCause.FALL)
			return;
		
		if (event.GetDamageeEntity().equals(GetEntity()))
			event.SetCancelled("Damage Cancel");
	}

	public void Die()
	{
		Loot();
		Remove();
	}

	public void Remove()
	{
		if (GetEntity() != null)
			GetEntity().remove();

		Event.CreatureDeregister(this);
	}

	public abstract void Loot();

	public String GetName()
	{
		return _name;
	}

	public double GetHealthCur()
	{
		if (_healthCur < 0)
			return 0;

		return _healthCur;
	}
	
	public double GetHealthMax()
	{
		return _healthMax;
	}

	public void ModifyHealth(double mod)
	{
		_healthCur += mod;

		if (_healthCur > _healthMax)
			_healthCur = _healthMax;

		if (_healthCur <= 0)
			Die();
		else
			UpdateName();
	}

	@EventHandler
	public void UpdateLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (GetEntity() == null)
			return;

		_loc = GetEntity().getLocation();
	}

	public Location GetLocation()
	{
		return _loc;
	}

	public EntityType GetType()
	{
		return _type;
	}

	public LivingEntity GetEntity()
	{
		return _entity;
	}

	public void SetEntity(LivingEntity ent)
	{
		_entity = ent;
	}

	public boolean CanExpire() 
	{
		return (System.currentTimeMillis() - _lastAttacked > 120000);
	}
}
