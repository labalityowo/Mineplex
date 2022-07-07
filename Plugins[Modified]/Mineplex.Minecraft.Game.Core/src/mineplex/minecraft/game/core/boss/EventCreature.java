package mineplex.minecraft.game.core.boss;

import java.util.UUID;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.world.ChunkUnloadEvent;

public abstract class EventCreature<T extends LivingEntity> implements Listener
{
	private WorldEvent _event;

	// Spawn Data
	private T _entity;
	private Class<? extends T> _entityClass;
	private Location _spawnLocation, _lastLocation;

	// Creature Data
	private String _name;
	private String _displayName;
	private boolean _useName;
	private double _health;
	private double _maxHealth;
	private boolean _showHealthName;
	private long _teleportHome;

	// Fight Data
	private long _lastDamaged;
	private UUID _lastDamager;

	public EventCreature(WorldEvent event, Location spawnLocation, String name, boolean useName, double health, Class<T> entityClass)
	{
		_event = event;

		_entityClass = entityClass;
		_spawnLocation = spawnLocation;

		_name = name;
		_displayName = name;
		_useName = useName;
		_health = health;
		_maxHealth = health;
		_showHealthName = true;
		_teleportHome = -1L;
	}
	
	public double getDifficulty()
	{
		return getEvent().getDifficulty();
	}

	protected void spawnEntity()
	{
		Location spawnLocation = _entity == null ? _spawnLocation : _entity.getLocation();
		T entity = _spawnLocation.getWorld().spawn(spawnLocation, getEntityClass());

		setEntity(entity);
		updateEntityHealth();
		entity.setRemoveWhenFarAway(false);
		spawnCustom();
		updateName();
	}

	protected abstract void spawnCustom();

	protected void updateEntityHealth()
	{
		_entity.setMaxHealth(500d);
		_entity.setHealth(500d);
	}

	protected void updateName()
	{
		String name = _displayName;

		if (_showHealthName)
		{
			String healthString = (int) _health + "/" + (int) _maxHealth;
			double per =_health / _maxHealth;
			if (per > 0.5)	healthString = C.cGreen + healthString;
			if (per > 0.2)	healthString = C.cYellow + healthString;
			else			healthString = C.cRed + healthString;

			name += " " + C.cWhite + "(" + healthString + C.cWhite + ")";
		}

		DisguiseBase disguise = getEvent().getDisguiseManager().getDisguise(getEntity());

		if (disguise != null && disguise instanceof DisguiseInsentient)
		{
			((DisguiseInsentient) disguise).setName(name);
			((DisguiseInsentient) disguise).setCustomNameVisible(_useName);
		}

		_entity.setCustomName(name);
		_entity.setCustomNameVisible(_useName);
	}

	public void remove()
	{
		remove(true);
	}

	public void remove(boolean removeFromEvent)
	{
		if (_entity != null)
		{
			if (getHealth() > 0)
			{
				_entity.remove();
			}
			else
			{
				_entity.setHealth(0);
				_entity.setCustomName("");
				_entity.setCustomNameVisible(false);
			}
		}

		if (removeFromEvent)
			_event.removeCreature(this);
	}

	protected final void die()
	{
		dieCustom();
		remove();
	}

	public abstract void dieCustom();

	public WorldEvent getEvent()
	{
		return _event;
	}

	public void setEvent(WorldEvent event)
	{
		_event = event;
	}

	public T getEntity()
	{
		return _entity;
	}

	public void setEntity(T entity)
	{
		if (_entity != null) _entity.remove();

		_entity = entity;
	}

	public Class<? extends T> getEntityClass()
	{
		return _entityClass;
	}

	public void setEntityClass(Class<? extends T> clazz)
	{
		_entityClass = clazz;
	}

	public double getHealthPercent()
	{
		return getHealth() / getMaxHealth();
	}

	public Location getSpawnLocation()
	{
		return _spawnLocation;
	}
	
	public Location getLastKnownLocation()
	{
		return _lastLocation;
	}

	public void setSpawnLocation(Location spawnLocation)
	{
		_spawnLocation = spawnLocation;
	}

	public String getDisplayName()
	{
		return _displayName;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_displayName = name == null ? _name : name;
		updateName();
	}

	public boolean isUseName()
	{
		return _useName;
	}

	public void setUseName(boolean useName)
	{
		_useName = useName;
		updateName();
	}

	public void applyDamage(double damage)
	{
		setHealth(getHealth() - damage);
	}

	public double getHealth()
	{
		return _health;
	}

	public void setHealth(double health)
	{
		_health = health;

		if (_health <= 0)
			die();
		else
			updateName();
	}

	public double getMaxHealth()
	{
		return _maxHealth;
	}

	public void setMaxHealth(double maxHealth)
	{
		_maxHealth = maxHealth;
	}

	public void setShowHealthName(boolean showHealthName)
	{
		_showHealthName = showHealthName;
		updateName();
	}

	/**
	 * Events
	 */

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		if (_entity != null && _entity.getLocation().getChunk().equals(event.getChunk()))
			event.setCancelled(true);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (_entity == null || _entity.isDead() || !_entity.isValid())
		{
			System.out.println("Respawning " + getName() + " because it is null or dead");
			spawnEntity();
		}
		
		if (UtilMath.offset2d(_entity.getLocation(), _spawnLocation) > 30)
		{
			if (_teleportHome != -1 && System.currentTimeMillis() >= _teleportHome)
			{
				_entity.teleport(_spawnLocation);
				_teleportHome = -1;
			}
			else
			{
				_entity.setVelocity(UtilAlg.getTrajectory(_entity.getLocation(), _spawnLocation).normalize().multiply(2));
				if (_teleportHome == -1)
				{
					_teleportHome = System.currentTimeMillis() + 5000;
				}
			}
		}
		else
		{
			if (_teleportHome != -1)
			{
				_teleportHome = -1;
			}
		}
		
		_lastLocation = _entity.getLocation();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(CustomDamageEvent event)
	{
		if (_entity == null)
			return;

		if (!event.GetDamageeEntity().equals(_entity))
			return;

		updateEntityHealth();

		applyDamage(event.GetDamage());
		updateName();

		_event.updateLastActive();
	}

	@EventHandler
	public void damageType(CustomDamageEvent event)
	{
		if (_entity == null)
			return;

		if (!event.GetDamageeEntity().equals(_entity))
			return;

		DamageCause cause = event.GetCause();

		if (cause == DamageCause.FALL && !getEvent().getCondition().HasCondition(_entity, ConditionType.FALLING, null))
			event.SetCancelled("Cancel");

		if (cause == DamageCause.DROWNING || cause == DamageCause.SUFFOCATION)
			event.SetCancelled("Cancel");
	}

	@EventHandler
	public void cancelCombust(EntityCombustEvent event)
	{
		if (_entity == null)
			return;

		if (!event.getEntity().equals(_entity))
			return;

		event.setCancelled(true);
	}
}
