package nautilus.game.arcade.game.games.halloween2016.creatures;

import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftZombie;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.disguise.disguises.DisguiseHorse;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobPumpkinPrince extends CreatureBase<Skeleton> implements Listener
{
	
	private Halloween2016 Host;
	
	private Zombie _horse;
	private DisguiseHorse _horseDisguise;
	
	private boolean _AI = false;
	private boolean _invulnerable = false;
	
	private long _horseKick = 0;
	private static final long HORSE_KICK_DURATION = 1000;
	
	private static final double HORSE_KICK_DAMAGE = 5;
	
	private static final float HORSE_SPEED = 1.5f;
	
	private static final double STAGE_3_KNOCKBACK = 3;
	private static final double HORSE_KNOCKBACK = 3;
	
	private static final double HEALTH = 1000;

	
	public MobPumpkinPrince(Halloween2016 game, Location loc)
	{
		super(game, C.cRed + C.Bold + "Pumpkin Prince", Skeleton.class, loc);
		Host = game;
	}

	@Override
	public void SpawnCustom(Skeleton ent)
	{
		ent.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
		ent.getEquipment().setItemInHand(new ItemStack(Material.BOW));
		
		ent.setMaxHealth(HEALTH);
		ent.setHealth(ent.getMaxHealth());
		
		ent.setSkeletonType(SkeletonType.WITHER);
		
		
		super.Host.CreatureAllowOverride = true;
		_horse = ent.getWorld().spawn(ent.getLocation(), Zombie.class);
		super.Host.CreatureAllowOverride = false;
		
		_horseDisguise = new DisguiseHorse(_horse);
		_horseDisguise.setType(Variant.SKELETON_HORSE);
		
		super.Host.getArcadeManager().GetDisguise().disguise(_horseDisguise);
		
		UtilEnt.setStepHeight(ent, 1);
		UtilEnt.setStepHeight(_horse, 1);
		
		UtilEnt.setBoundingBox(_horse, 0, 0);

		//For some reason 'UtilEnt.silence' doesn't work, so using native silence instead
		//UtilEnt.silence(_horse, true);
		((CraftZombie)_horse).getHandle().b(true);
		
		_horse.setPassenger(ent);
		UtilEnt.vegetate(_horse);
		
		UtilServer.RegisterEvents(this);
		
		UtilEnt.setTickWhenFarAway(ent, true);
		ent.setRemoveWhenFarAway(false);
		UtilEnt.setTickWhenFarAway(_horse, true);
		_horse.setRemoveWhenFarAway(false);
		
		addEntityPart(_horse);
	}
	
	public void setInvulnerable(boolean invulnerable)
	{
		_invulnerable = invulnerable;
	}
	
	public boolean isInvulnerable()
	{
		return _invulnerable;
	}
	
	public void setAI(boolean AI)
	{
		_AI = AI;
	}
	
	public boolean getAI()
	{
		return _AI;
	}

	@Override
	public void Update(UpdateEvent event)
	{
		if(!_AI) return;
		
		if(event.getType() != UpdateType.TICK) return;
		
		UtilTextTop.displayProgress(C.cYellow + C.Bold + "Pumpkin Prince", getHealthProgress(), UtilServer.getPlayers());
		
		int state = getState();
		
		if(_horse.isValid() && state > 1)
		{
			_horse.getWorld().playSound(_horse.getLocation(), Sound.BLAZE_DEATH, 3, 1);
			UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, _horse.getLocation(), null, 0.5f, 20, ViewDist.NORMAL);
			_horse.setHealth(0);
		}
		
		if(state == 1)
		{
			Entity target = GetEntity().getTarget();
			if(target != null)
			{
				UtilEnt.CreatureMove(_horse, GetEntity().getTarget().getLocation(), HORSE_SPEED);
								
				boolean hit = false;
				
				for(Player p : getInsideBoundingBox())
				{
					CustomDamageEvent dmgEvent = Host.getArcadeManager().GetDamage().NewDamageEvent(p, _horse, null, GetEntity().getLocation(), DamageCause.ENTITY_ATTACK, HORSE_KICK_DAMAGE, true, false, false, "Pumpkin Prince", "Horse Kick", false);
					if(dmgEvent.isCancelled()) continue;
					hit = true;
				}
				if(hit)
				{
					_horseKick = System.currentTimeMillis();
					
					_horseDisguise.kick();
					Host.getArcadeManager().GetDisguise().updateDisguise(_horseDisguise);
					
					_horse.getWorld().playSound(_horse.getLocation(), Sound.SKELETON_HURT, 4f, 0.6f);
					_horse.getWorld().playSound(_horse.getLocation(), Sound.SKELETON_HURT, 4f, 0.6f);
				}
				
				if(_horseKick + HORSE_KICK_DURATION < System.currentTimeMillis())
				{
					_horseDisguise.stopKick();
					Host.getArcadeManager().GetDisguise().updateDisguise(_horseDisguise);
				}
				
				if(GetEntity().getTicksLived() % 15 == 0)
				{
					if(target.getLocation().distanceSquared(GetEntity().getLocation()) > 40*40) return;

					Arrow a = GetEntity().launchProjectile(Arrow.class);
					
					Vector force = UtilAlg.getTrajectory(GetEntity(), target).add(new Vector(0, 0.2, 0));
					a.setVelocity(force);

					a.setFireTicks(99999);
				}

			}
			else
			{
				GetEntity().setTarget(getRandomPlayer());
			}
		}
		else if(state == 2)
		{
			if(GetEntity().getTicksLived() % (20*5) == 0)
			{
				for(int i = 0; i < 64; i++)
				{
					if(tryRandomTeleport()) break;
				}
			}
			if(GetEntity().getTicksLived() % 25 == 0)
			{
				Vector v = GetPlayerTarget().subtract(GetEntity().getLocation()).toVector().multiply(0.01);
				
				Location loc = GetEntity().getLocation();
				loc.setDirection(v);
				GetEntity().teleport(loc);
				GetEntity().launchProjectile(Fireball.class, v);
			}
		}
		else if(state == 3)
		{
			if(GetEntity().getTicksLived() % 20*10 == 0)
			{
				GetEntity().setTarget(getRandomPlayer());
			}
		}
	}
	
	public Player getRandomPlayer()
	{
		return Host.GetPlayers(true).get(UtilMath.r(Host.GetPlayers(true).size()));
	}
	
	private boolean tryRandomTeleport()
	{
		Location loc = GetPlayerTarget();
		loc.add((UtilMath.random.nextDouble() - 0.5) * 64, UtilMath.r(64)-32, (UtilMath.random.nextDouble() - 0.5) * 64);
		
		for(int y = loc.getBlockY(); y > 0; y--)
		{
			if(!loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) continue;
			Material m1 = loc.getBlock().getType();
			Material m2 = loc.getBlock().getRelative(BlockFace.UP).getType();
			if(m1.isSolid()) continue;
			if(m2.isSolid()) continue;
			if(UtilItem.isLiquid(m1)) continue;
			if(UtilItem.isLiquid(m2)) continue;
			
			loc.getWorld().playEffect(GetEntity().getLocation(), Effect.ENDER_SIGNAL, 0);
			loc.getWorld().playSound(GetEntity().getLocation(), Sound.ENDERMAN_TELEPORT, 2, 0);
			GetEntity().teleport(loc.getBlock().getLocation().add(0.5, 0, 0.5));
			loc.getWorld().playEffect(GetEntity().getLocation(), Effect.ENDER_SIGNAL, 0);
			loc.getWorld().playSound(GetEntity().getLocation(), Sound.ENDERMAN_TELEPORT, 2, 0);
			return true;
		}
		return false;
		
	}
	
	@EventHandler
	public void onShoot(EntityShootBowEvent event)
	{
		if(!event.getEntity().equals(GetEntity())) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDismount(EntityDismountEvent event)
	{
		if(event.getDismounted().equals(GetEntity())) event.setCancelled(true);
	}
	
	public Zombie getHorse()
	{
		return _horse;
	}

	@Override
	public void Damage(CustomDamageEvent event)
	{
		if(event.GetDamageeEntity().equals(_horse))
		{
			event.setDamagee(GetEntity());
			_horse.setFireTicks(0);
		}
		
		if(event.GetDamageeEntity() instanceof Player)
		{
			if(GetEntity().equals(event.GetDamagerEntity(true)))
			{
				onPrinceDamagePlayer(event);
			}
			else
			{
				onOtherDamagePlayer(event);
			}
		}
		
		if(GetEntity().equals(event.GetDamageeEntity()))
		{
			onPrinceTakeDamage(event);
			
			if(!event.isCancelled() && _horse != null && _horse.isValid())
			{
				PacketPlayOutAnimation hurt = new PacketPlayOutAnimation();
				hurt.a = _horseDisguise.getEntityId();
				hurt.b = 1;
				_horseDisguise.sendPacket(hurt);
			}
		}
	}
	
	public void onPrinceTakeDamage(CustomDamageEvent event)
	{
		if(event.GetCause() == DamageCause.SUFFOCATION || event.GetCause() == DamageCause.FALL)
		{
			event.SetCancelled("Boss Invalid Damage");
			return;
		}
		
		if(GetEntity().equals(event.GetDamagerEntity(true)))
		{
			event.SetCancelled("Boss Invalid Damage");
			return;
		}
		
		if(!_AI) 
		{
			event.SetCancelled("AI Disabled");
			return;
		}
		
		if(_invulnerable)
		{
			event.SetCancelled("Invulnerable");
			Player p = event.GetDamagerPlayer(true);
			if(p != null)
			{
				if(Recharge.Instance.use(p, "Boss Inulnerable Info", 5000, false, false))
				{
					p.sendMessage(F.main("Game", "Kill the " + F.item("Guards") + " before you can damage the " + F.color("Pumpkin Prince", C.cYellow + C.Bold)));
				}
			}
			return;
		}
		
		if(event.GetProjectile() instanceof Snowball)
		{
			event.SetKnockback(false);
		}
		
		updateHealth(GetEntity().getHealth(), GetEntity().getHealth()-event.GetDamage());
	}
	
	public void onPrinceDamagePlayer(CustomDamageEvent event)
	{
		int state = getState();
		
		if(state == 3)
		{
			event.AddKnockback("Boss Knockback", STAGE_3_KNOCKBACK);
		}
	}
	
	public void onOtherDamagePlayer(CustomDamageEvent event)
	{
		if(_horse.equals(event.GetDamagerEntity(false)))
		{
			event.AddKnockback("Horse Kick", HORSE_KNOCKBACK);
		}
	}
	
	public void updateHealth(double oldH, double newH)
	{
		int oldState = getState(getHealthProgress(oldH));
		int newState = getState(getHealthProgress(newH));
		if(oldState != newState)
		{
			updateState(newState);
		}
	}
	
	public void updateState(int state)
	{
		if(state == 1)
		{
			
		}
		if(state == 2)
		{
			GetEntity().getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
			
			GetEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			GetEntity().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
		}
		if(state == 3)
		{
			Host.setMaxPumplings(0);
			
			GetEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
			GetEntity().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2), true);
		}
	}
	
	@Override
	public void remove()
	{
		die();
		super.remove();
	}
	
	public void die()
	{
		UtilServer.Unregister(this);
	}

	@Override
	public void Target(EntityTargetEvent event)
	{
		if(!event.getEntity().equals(GetEntity())) return;
		
		if(!_AI) 
		{
			event.setCancelled(true);
			return;
		}
		
		if(event.getTarget() == null) 
		{
			event.setCancelled(true);
			return;
		}
		
		if(event.getTarget().getType() != EntityType.PLAYER) event.setCancelled(true);
	}
	
	public int getState()
	{
		return getState(getHealthProgress());
	}
	
	public int getState(double healthProgress)
	{
		if(healthProgress > 0.6) return 1;
		if(healthProgress > 0.4) return 2;
		return 3;
	}
	
	public double getHealthProgress(double health)
	{
		return health / GetEntity().getMaxHealth();
	}
	
	public double getHealthProgress()
	{
		return getHealthProgress(GetEntity().getHealth());
	}
	
	public boolean isDead()
	{
		return getHealthProgress() <= 0;
	}


}
