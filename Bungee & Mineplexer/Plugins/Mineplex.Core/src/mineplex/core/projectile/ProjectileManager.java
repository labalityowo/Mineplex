package mineplex.core.projectile;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ProjectileManager extends MiniPlugin
{
	private WeakHashMap<Entity, ProjectileUser> _thrown = new WeakHashMap<Entity, ProjectileUser>();
	
	public ProjectileManager(JavaPlugin plugin) 
	{
		super("Throw", plugin);
	}
	
	/**
	 * @param thrown - the thrown {@link Entity} whose thrower is to be fetched
	 * @return the {@link LivingEntity} that threw the {@code thrown} entity, if one
	 * can be found, null otherwise.
	 */
	public LivingEntity getThrower(Entity thrown)
	{
		if (_thrown.containsKey(thrown))
		{
			return _thrown.get(thrown).getThrower();
		}
		
		return null;
	}
	
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback, 
			long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle, float hitboxGrow)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback, 
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				null, 1f, 1f, null, 0, null, null, 0F, 0F, 0F, 0F, 1, hitboxGrow));  
	}
	
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback, 
			long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle, boolean pickup, float hitboxGrow)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback, 
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, pickup, 
				null, 1f, 1f, null, 0, null, null, 0F, 0F, 0F, 0F, 1, hitboxGrow)); 
	}
		
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback, 
			long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
			Sound sound, float soundVolume, float soundPitch, Effect effect, int effectData, UpdateType effectRate , float hitboxGrow)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback, 
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, effect, effectData, effectRate, null, 0F, 0F, 0F, 0F, 1, hitboxGrow)); 
	}
	
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback, 
			long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
			Sound sound, float soundVolume, float soundPitch, ParticleType particle, Effect effect, int effectData, UpdateType effectRate, float hitboxGrow)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback, 
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, effect, effectData, effectRate, particle, 0F, 0F, 0F, 0F, 1, hitboxGrow)); 
	}
	
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback, 
			long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
			Sound sound, float soundVolume, float soundPitch, ParticleType particle, UpdateType effectRate, float hitboxMult)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback, 
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, null, 0, effectRate, particle, 0F, 0F, 0F, 0F, 1, hitboxMult)); 
	}
	
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback, 
			long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
			Sound sound, float soundVolume, float soundPitch, ParticleType particle, UpdateType effectRate, float hitboxMult, double charge)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback, 
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, null, 0, effectRate, particle, 0F, 0F, 0F, 0F, 1, hitboxMult, charge)); 
	}
	
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback, 
			long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
			Sound sound, float soundVolume, float soundPitch, ParticleType particle, float pX, float pY, float pZ, float pS, int pC, UpdateType effectRate, float hitboxMult)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback,
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, null, 0, effectRate, particle, pX, pY, pZ, pS, pC, hitboxMult)); 
	}

	// WITH CAN HIT PLAYERS LIST:
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback,
						 long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle, float hitboxGrow, List<Player> canHit)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback,
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				null, 1f, 1f, null, 0, null, null, 0F, 0F, 0F, 0F, 1, hitboxGrow, canHit));
	}

	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback,
						 long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle, boolean pickup, float hitboxGrow, List<Player> canHit)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback,
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, pickup,
				null, 1f, 1f, null, 0, null, null, 0F, 0F, 0F, 0F, 1, hitboxGrow, canHit));
	}

	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback,
						 long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
						 Sound sound, float soundVolume, float soundPitch, Effect effect, int effectData, UpdateType effectRate , float hitboxGrow, List<Player> canHit)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback,
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, effect, effectData, effectRate, null, 0F, 0F, 0F, 0F, 1, hitboxGrow, canHit));
	}

	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback,
						 long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
						 Sound sound, float soundVolume, float soundPitch, ParticleType particle, Effect effect, int effectData, UpdateType effectRate, float hitboxGrow, List<Player> canHit)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback,
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, effect, effectData, effectRate, particle, 0F, 0F, 0F, 0F, 1, hitboxGrow, canHit));
	}

	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback,
			 long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
			 Sound sound, float soundVolume, float soundPitch, ParticleType particle, UpdateType effectRate, float hitboxMult, List<Player> canHit)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback,
			expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
			sound, soundVolume, soundPitch, null, 0, effectRate, particle, 0F, 0F, 0F, 0F, 1, hitboxMult, canHit));
	}
	
	public void AddThrow(Entity thrown, LivingEntity thrower, IThrown callback,
						 long expireTime, boolean hitPlayer, boolean hitNonPlayerEntity, boolean hitBlock, boolean idle,
						 Sound sound, float soundVolume, float soundPitch, ParticleType particle, float pX, float pY, float pZ, float pS, int pC, UpdateType effectRate, float hitboxMult, List<Player> canHit)
	{
		_thrown.put(thrown, new ProjectileUser(this, thrown, thrower, callback,
				expireTime, hitPlayer, hitNonPlayerEntity, hitBlock, idle, false,
				sound, soundVolume, soundPitch, null, 0, effectRate, particle, pX, pY, pZ, pS, pC, hitboxMult, canHit));
	}
	
	public void deleteThrown(Entity thrown)
	{
		_thrown.remove(thrown);
		thrown.remove();
	}
		
	@EventHandler
	public void Update(UpdateEvent event)
	{
		//BouncyCollisions
		if (event.getType() == UpdateType.TICK)
		{
			for (Iterator<Entry<Entity, ProjectileUser>> iterator = _thrown.entrySet().iterator(); iterator.hasNext();)
			{
				Entry<Entity, ProjectileUser> entry = iterator.next();
				Entity cur = entry.getKey();
				
				if (cur.isDead() || !cur.isValid())
				{
					entry.getValue().getIThrown().Idle(entry.getValue());
					iterator.remove();
					continue;
				}
				else if (_thrown.get(cur).collision())
					iterator.remove();
			}
		}
		
		//Effects
		for (ProjectileUser cur : _thrown.values())
			cur.effect(event);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void Pickup(PlayerPickupItemEvent event)
	{
		if (event.isCancelled())
			return;

		if (_thrown.containsKey(event.getItem()))
			if (!_thrown.get(event.getItem()).canPickup(event.getPlayer()))
				event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void HopperPickup(InventoryPickupItemEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (_thrown.containsKey(event.getItem()))
			event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void chunkUnload(ChunkUnloadEvent event)
	{
		for (Entity e : event.getChunk().getEntities())
		{
			if (_thrown.containsKey(e))
			{
				_thrown.get(e).chunkUnload();
			}
		}
	}
}