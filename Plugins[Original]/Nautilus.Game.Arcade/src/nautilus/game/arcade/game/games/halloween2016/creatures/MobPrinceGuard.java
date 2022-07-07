package nautilus.game.arcade.game.games.halloween2016.creatures;

import java.util.List;

import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguiseHorse;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;

public class MobPrinceGuard extends CreatureBase<Skeleton>
{
	
	private Zombie _horse;
	private DisguiseHorse _horseDisguise;
	
	private MobPumpkinPrince _prince;
	private Entity _target;
	
	private Location _strikeTarget;
	private long _strikeStamp;
	
	private boolean _AI = true;
	
	private final static float HORSE_SPEED = 1.8f;
	private final static float HORSE_SPEED_CHARGE = 2.3f;
	private final static double STRIKE_RANGE = 10;
	private final static long MAX_STRIKE_TIME = 10_000;
	private final static long STRIKE_COOLDOWN = 15_000;
	private final static double DAMAGE = 5;
	private final static double KNOCKBACK = 3;
	private final static double HEALTH = 50;

	public MobPrinceGuard(Halloween game, Location loc, MobPumpkinPrince prince)
	{
		super(game, C.cYellow + C.Bold + "Prince Guard", Skeleton.class, loc);
		_prince = prince;
	}
	
	public Zombie getHorse()
	{
		return _horse;
	}

	@Override
	public void SpawnCustom(Skeleton ent)
	{
		ent.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD));
		ent.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
		
		_horse = ent.getWorld().spawn(ent.getLocation(), Zombie.class);
		
		_horseDisguise = new DisguiseHorse(_horse);
		_horseDisguise.setType(Variant.SKELETON_HORSE);
		Host.getArcadeManager().GetDisguise().disguise(_horseDisguise);
		
		_horse.setRemoveWhenFarAway(false);
		UtilEnt.setTickWhenFarAway(_horse, true);
		
		UtilEnt.setStepHeight(_horse, 1);
		UtilEnt.setBoundingBox(_horse, 0, 0);
		
		UtilEnt.setTickWhenFarAway(ent, true);
		ent.setRemoveWhenFarAway(true);
		
		ent.setMaxHealth(HEALTH);
		ent.setHealth(ent.getMaxHealth());
		
		_horse.setPassenger(ent);
		
		_horse.setVegetated(true);
		//UtilEnt#silence doesn't seem to work. Using native silence instead
		((CraftEntity)_horse).getHandle().b(true);
		
		addEntityPart(_horse);
	}
	
	public void setAI(boolean aI)
	{
		_AI = aI;
	}
	
	public boolean getAI()
	{
		return _AI;
	}

	@Override
	public void Update(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		
		if(!_AI) return;
		
		if(_strikeTarget != null)
		{
			if(_strikeStamp < System.currentTimeMillis())
			{
				_strikeTarget = null;
			}
			else if(UtilEnt.canEntityWalkCloserToNavigationTarget(_horse))
			{
				_strikeTarget = null;
				return;
			}
			else
			{
				_horse.getWorld().playSound(_horse.getLocation(), Sound.FIZZ, 0.2f, 1f);
				_horse.getWorld().playSound(_horse.getLocation(), Sound.SKELETON_WALK, 0.2f, 0.5f);
				
				UtilParticle.PlayParticleToAll(ParticleType.SMOKE, _horse.getLocation(), null, 0.1f, 5, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.FLAME, _horse.getLocation(), null, 0.1f, 5, ViewDist.NORMAL);
			}
		}
		
		List<Player> inGame = Host.GetPlayers(true);
		if(_target == null || !inGame.contains(_target))
		{
			double dist = 0;
			for(Player p : inGame)
			{
				double d = p.getLocation().distanceSquared(_prince.getHorse().getLocation());
				if(_target == null || dist > d)
				{
					dist = d;
					_target = p;
				}
			}
		}
		Location target = _target.getLocation();
		Vector diff = target.clone().subtract(_horse.getLocation()).toVector();
		if(_strikeStamp + STRIKE_COOLDOWN > System.currentTimeMillis() || diff.lengthSquared() >= STRIKE_RANGE*STRIKE_RANGE)
		{
			diff.normalize().multiply(STRIKE_RANGE*2);
			target.add(diff);
			SetTarget(target);
			
			UtilEnt.CreatureMove(_horse, GetTarget(), HORSE_SPEED);
		}
		else
		{
			_target = null;
			
			_strikeTarget = target;
			_strikeStamp = System.currentTimeMillis() + MAX_STRIKE_TIME;
			
			UtilEnt.CreatureMove(_horse, GetTarget(), HORSE_SPEED_CHARGE);
		}
		
		for(Player p : getInsideBoundingBox())
		{
			Host.getArcadeManager().GetDamage().NewDamageEvent(p, _horse, null, _horse.getLocation(), DamageCause.ENTITY_ATTACK, DAMAGE, true, false, false, "Prince Guard", "Horse Kick", false);
		}
	}

	@Override
	public void Damage(CustomDamageEvent event)
	{
		if(_horse.equals(event.GetDamageeEntity()))
		{
			event.setDamagee(GetEntity());
		}
		
		if(GetEntity().equals(event.GetDamageeEntity()))
		{
			if(_prince.equals(event.GetDamagerEntity(true)))
			{
				if(event.GetProjectile() != null)
				{
					event.GetProjectile().setFireTicks(0);
				}
				event.SetCancelled("Cancel");
				return;
			}
			
			if(!_AI)
			{
				event.SetCancelled("No AI");
				return;
			}
			
			if(!event.isCancelled() && _horse != null && _horse.isValid())
			{
				PacketPlayOutAnimation hurt = new PacketPlayOutAnimation();
				hurt.a = _horseDisguise.getEntityId();
				hurt.b = 1;
				_horseDisguise.sendPacket(hurt);
			}
		}
		
		else if(event.GetDamageeEntity() instanceof Player)
		{
			if(_horse.equals(event.GetDamagerEntity(false)))
			{
				event.AddKnockback("Horse Knockback", KNOCKBACK);
			}
		}
	}

	@Override
	public void Target(EntityTargetEvent event)
	{}
	
	@EventHandler
	public void onDismount(EntityDismountEvent event)
	{
		if(event.getDismounted().equals(GetEntity())) event.setCancelled(true);
	}
	

}
