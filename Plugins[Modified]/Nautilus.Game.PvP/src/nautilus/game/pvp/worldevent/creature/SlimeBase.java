package nautilus.game.pvp.worldevent.creature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMobBoss;

public abstract class SlimeBase extends EventMobBoss
{
	//FLAGS
	public int speed = 0;
	public int loot = 0;

	public int shieldCount = 0;
	private long _shieldLast = 0;
	private ArrayList<SlimeShield> _shields = new ArrayList<SlimeShield>();

	public int rocketCount = 0;
	private HashMap<LivingEntity, Integer> _rocketReturn = new HashMap<LivingEntity, Integer>();
	
	private double _damageLoot = 20;
	private double _damageToLoot = 0;

	public SlimeBase(EventBase event, Location location, String name, int health) 
	{
		super(event, location, name, true, health, EntityType.SLIME);
		
		_attackerDelay = 800;
	}

	public abstract void SpawnCustom();

	@Override
	public void DamagedCustom(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.FALL)
		{
			event.SetCancelled("Fall Immunity");
			return;
		}
		
		event.SetKnockback(false);

		_damageToLoot += event.GetDamage();

		while (_damageToLoot >= _damageLoot)
		{
			Event.Manager.Loot().DropEmerald(GetEntity().getLocation(), 1, 0, 2d);
			_damageToLoot -= _damageLoot;
		}	
	}

	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 30, 1, 
				Sound.SLIME_WALK, 2f, 0.6f, Material.SLIME_BALL, (byte)0, false);

		Split();
		Loot();
		Remove();
	}


	public abstract void Split();
	
	public void Loot()
	{
		Event.Manager.Loot().DropLoot(GetEntity().getEyeLocation(), loot, loot, 0.05f, 0.03f, 2d);
	}

	public void RocketLaunch(LivingEntity target) 
	{
		Event.CreatureRegister(new SlimeRocket(Event, GetEntity().getEyeLocation(), target));
	}
	
	@EventHandler
	public void SlimeSpeed(UpdateEvent event)
	{
		if (speed < 1)
			return;

		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		Event.Manager.Condition().Factory().Speed("Slime Speed", GetEntity(), GetEntity(), 1.9, speed-1, false, false);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void RocketCounter(CustomDamageEvent event)
	{
		if (rocketCount == 0)
			return;

		if (event.IsCancelled())
			return;

		if (GetEntity() == null)
			return;

		if (event.GetProjectile() == null)
			return;

		if (!event.GetDamageeEntity().equals(GetEntity()))
			return;

		LivingEntity damager = event.GetDamagerEntity(true);
		if (damager == null)	return;

		//Return Fire
		_rocketReturn.put(damager, rocketCount);
	}

	@EventHandler
	public void RocketUpdate(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		HashSet<LivingEntity> remove = new HashSet<LivingEntity>();

		for (LivingEntity target : _rocketReturn.keySet())
		{
			int count = _rocketReturn.get(target);

			RocketLaunch(target);

			if (count > 1)
				_rocketReturn.put(target, count - 1);
			else
				remove.add(target);
		}

		for (LivingEntity cur : remove)
			_rocketReturn.remove(cur);
	}

	@EventHandler
	public void ShieldSpawn(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.FAST)
			return;
		
		if (_shields.size() >= shieldCount)
			return;

		if (System.currentTimeMillis() < _shieldLast)
			return;

		//Regen One
		if (!_shields.isEmpty())
			Event.CreatureRegister(new SlimeShield(Event, GetEntity().getLocation(), this));
		
		//Regen Fully
		else
			for (int i=0 ; i<shieldCount ; i++)
				Event.CreatureRegister(new SlimeShield(Event, GetEntity().getLocation(), this));	
	}

	@EventHandler(priority = EventPriority.LOW)
	public void ShieldDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (_shields.isEmpty())
			return;

		if (GetEntity() == null)
			return;

		if (!event.GetDamageeEntity().equals(GetEntity()))
			return;

		event.SetCancelled("Slime Shield");

		UtilPlayer.message(event.GetDamagerPlayer(true), F.main("World Event", 
				F.name(GetName()) + " is protected by " + F.skill("Slime Shield") + "."));
	}

	public void ShieldRegister(SlimeShield slimeShield) 
	{
		_shields.add(slimeShield);
		
		ShieldPositions();
	}

	public void ShieldDeregister(SlimeShield slimeShield) 
	{
		_shields.remove(slimeShield);
		
		ShieldPositions();
		
		if (!_shields.isEmpty())			
			_shieldLast = System.currentTimeMillis() + 15000;
		
		else					
		{
			_shieldLast = System.currentTimeMillis() + 45000;
			
			for (Player player : UtilPlayer.getNearby(GetEntity().getLocation(), 32))
				UtilPlayer.message(player, F.main("World Event", F.name(GetName()) + " lost " + F.skill("Slime Shield") +  " for " + F.time("30 Seconds") + "."));
		}
			
	}
	
	public void ShieldPositions()
	{
		int i = 0;
		for (SlimeShield cur : _shields)
		{
			cur.SetRadialLead(i * ((2d * Math.PI)/_shields.size()));
			i++;
		}	
	}
	
	@Override
	public void DistanceAction() 
	{

	}
}
