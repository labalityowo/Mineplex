package mineplex.game.clans.clans.nether.miniboss;

import java.util.Arrays;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Base class for nether minibosses
 * @param <Mob> The type of entity this boss will use
 */
public abstract class NetherMiniBoss<Mob extends LivingEntity> implements Listener
{
	private Mob _entity;
	private EntityType _type;
	private String _name;
	private double _maxHealth;
	private Location _spawn;
	
	public NetherMiniBoss(String displayName, Double maxHealth, Location spawn, EntityType type)
	{
		_name = displayName;
		_maxHealth = maxHealth;
		_spawn = spawn;
		_type = type;
		
		spawn();
	}
	
	@SuppressWarnings("unchecked")
	private void spawn()
	{
		_entity = (Mob) _spawn.getWorld().spawnEntity(_spawn, _type);
		_entity.setMaxHealth(_maxHealth);
		_entity.setHealth(_maxHealth);
		_entity.setCustomName(_name);
		_entity.setCustomNameVisible(true);
		
		customSpawn();
		Bukkit.getPluginManager().registerEvents(this, ClansManager.getInstance().getPlugin());
	}
	
	/**
	 * Fetches the entity for this boss
	 * @return The entity for this boss
	 */
	public Mob getEntity()
	{
		return _entity;
	}
	
	/**
	 * Method called after the entity spawns
	 */
	public void customSpawn() {};
	
	/**
	 * Method called when the entity dies
	 * @param deathLocation The location where the entity died
	 */
	public void customDeath(Location deathLocation) {};
	
	/**
	 * Method called when the entity despawns for non-death reasons
	 */
	public void customDespawn() {};
	
	/**
	 * Method called for updating every 10 ticks
	 */
	public void update() {};
	
	@EventHandler
	public void onDeath(EntityDeathEvent event)
	{
		if (event.getEntity().equals(_entity))
		{
			event.setDroppedExp(0);
			event.getDrops().clear();
			HandlerList.unregisterAll(this);
			customDeath(event.getEntity().getLocation());
		}
	}
	
	@EventHandler
	public void onUnload(ChunkUnloadEvent event)
	{
		if (Arrays.asList(event.getChunk().getEntities()).contains(_entity))
		{
			HandlerList.unregisterAll(this);
			_entity.remove();
			customDespawn();
			return;
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		if (!_entity.isValid())
		{
			HandlerList.unregisterAll(this);
			customDespawn();
			return;
		}
		
		if (_entity.getFireTicks() > 0)
		{
			_entity.setFireTicks(-1);
		}
		update();
	}
}