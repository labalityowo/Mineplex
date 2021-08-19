package nautilus.game.arcade.game.games.halloween2016.creatures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseWitch;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import net.minecraft.server.v1_8_R3.RandomPositionGenerator;
import net.minecraft.server.v1_8_R3.Vec3D;

public class MobWitch extends CreatureBase<Zombie>
{
	private static int HEALTH = 20;
	private static int PANIC_TIME = 100;
	
	private float SPEED_IDLE = 1f;
	private float SPEED_PANIC = 2f;
	
	private static int BATS_BURST = 10;
	private static int BATS_TICKS = 80;
	private static double BAT_KNOCKBACK = 1.75;
	private static int BAT_FIRE_TICKS = 10;
	private static double BAT_DAMAGE = 3;
	
	private static double HEAL_DISTANCE = 7;
	private static double HEAL_AMOUNT = 10;
	private static double HEAL_RAY_SPEED = 0.2;
	
	
	
	private CreatureBase<?> _healTarget = null;
	public Location _healRay = null;
	
	private int _panicTicks = 0;
	private List<Bat> _bats = new ArrayList<>();
	
	private Location _panicTarget = null;
	
	private Halloween2016 _host;
	
	public MobWitch(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow  + "Witch", Zombie.class, loc);
		_host = game;
	}

	@Override
	public void SpawnCustom(Zombie ent)
	{
		ent.setMaxHealth(HEALTH);
		ent.setHealth(ent.getMaxHealth());
		
		UtilEnt.setTickWhenFarAway(ent, true);
		ent.setRemoveWhenFarAway(false);
		
		DisguiseWitch disg = new DisguiseWitch(ent);
		Host.getArcadeManager().GetDisguise().disguise(disg);
	}

	@Override
	public void Update(UpdateEvent event)
	{
		if(event.getType() == UpdateType.TICK)
		{
			updateBats();
			move();
		}
	}

	@Override
	public void Damage(CustomDamageEvent event)
	{
		if(event.isCancelled()) return;
		
		if(_bats.contains(event.GetDamageeEntity()))
		{
			if(event.GetCause() == DamageCause.FIRE_TICK)
			{
				event.SetCancelled("Fire Imunity");
				return;
			}
		}
		if(event.GetDamageeEntity() instanceof Player)
		{
			if (event.GetReason() != null && event.GetReason().contains(GetName()))
			{
				event.AddKnockback(GetName(), BAT_KNOCKBACK);
				return;
			}
		}
		if(!GetEntity().equals(event.GetDamageeEntity())) return;
		
		LivingEntity damager = event.GetDamagerEntity(false);
		
		if(damager != null && _panicTicks <= 0)
		{
			shootBats(damager.getEyeLocation().subtract(GetEntity().getEyeLocation()).toVector());
		}
		
		_panicTicks = PANIC_TIME;
	}
	
	public void shootBats(Vector direction)
	{
		Location loc = GetEntity().getLocation();
		loc.setDirection(direction);
		GetEntity().teleport(loc);
		Host.CreatureAllowOverride = true;
		for(int i = 0; i < BATS_BURST; i++)
		{
			Bat bat = GetEntity().getWorld().spawn(GetEntity().getEyeLocation(), Bat.class);
			UtilEnt.vegetate(bat);
			_bats.add(bat);
			addEntityPart(bat);
		}
		Host.CreatureAllowOverride = false;
	}

	@Override
	public void Target(EntityTargetEvent event)
	{}
	
	public void move()
	{
		if(_panicTicks > 0)
		{
			movePanic();
			_panicTicks--;
		}
		else
		{
			_panicTarget = null;
			moveIdle();
		}
	}
	
	public void updateBats()
	{
		for (Iterator<Bat> it = _bats.iterator(); it.hasNext();)
		{
			Bat bat = it.next();
			if (!bat.isValid())
			{
				it.remove();
				continue;
			}
			
			if(bat.getTicksLived() > BATS_TICKS)
			{
				bat.remove();
				it.remove();
				UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bat.getLocation(), 0, 0, 0, 0, 3,
						ViewDist.LONG, UtilServer.getPlayers());
				continue;
			}
			
			bat.setFireTicks(10);
			
			Vector rand = new Vector((Math.random() - 0.5)/2, (Math.random() - 0.5)/2, (Math.random() - 0.5)/2);
			bat.setVelocity(bat.getLocation().getDirection().multiply(0.5).add(rand));
			
			for(Player p : UtilEnt.getPlayersInsideEntity(bat, Host.GetPlayers(true)))
			{	
				if (!Recharge.Instance.usable(p, "Hit by Bat"))
					continue;
				
					//Damage Event
					Host.getArcadeManager().GetDamage().NewDamageEvent(p, GetEntity(), null, 
							DamageCause.CUSTOM, BAT_DAMAGE, true, true, false,
							GetEntity().getName(), GetName());
					p.setFireTicks(p.getFireTicks() + BAT_FIRE_TICKS);
					
					//Effect
					bat.getWorld().playSound(bat.getLocation(), Sound.BAT_HURT, 1f, 1f);
					UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bat.getLocation(), 0, 0, 0, 0, 3,
							ViewDist.LONG, UtilServer.getPlayers());
					
					bat.remove();
					it.remove();
					
					//Recharge on hit
					Recharge.Instance.useForce(p, "Hit by Bat", 200);
			}
		}
	}
	
	public void getNewHealTarget()
	{
		CreatureBase<?> close = null;
		double dist = -1;
		for(CreatureBase<?> c : _host.getNonPumplings())
		{
			if(c.equals(this)) continue;
			if(!c.GetEntity().isValid()) continue;
			
			if(c.GetEntity().getHealth() < c.GetEntity().getMaxHealth())
			{
				double d = GetEntity().getLocation().distanceSquared(c.GetEntity().getLocation());
				if(close == null || d < dist)
				{
					close = c;
					dist = d;
				}
			}
		}
		_healTarget = close;
	}
	
	public void moveIdle()
	{
		if(_healTarget != null && !_healTarget.GetEntity().isValid())
		{
			_healTarget = null;
		}
		if(_healTarget == null)
		{
			getNewHealTarget();
		}
		
		if(_healTarget == null)
		{
			UtilEnt.CreatureMove(GetEntity(), _host.getInfrontOfDoorTargets().get(0), SPEED_IDLE);
			
			return;
		}
		
		if(_healRay != null)
		{
			Vector diff = _healTarget.GetEntity().getEyeLocation().subtract(_healRay).toVector();
			double length = diff.lengthSquared();
			if(length <= HEAL_RAY_SPEED * HEAL_RAY_SPEED)
			{
				LivingEntity ent = _healTarget.GetEntity();
				ent.setHealth(Math.min(ent.getMaxHealth(), ent.getHealth()+HEAL_AMOUNT));
				
				UtilParticle.PlayParticleToAll(ParticleType.HEART, ent.getEyeLocation(), 1, 1, 1, 0, 20, ViewDist.NORMAL);
				
				_healTarget = null;
				_healRay = null;
				
				return;
			}
			diff.normalize().multiply(HEAL_RAY_SPEED);
			
			_healRay.add(diff);
			UtilParticle.PlayParticleToAll(ParticleType.HEART, _healRay, 0, 0, 0, 0, 1, ViewDist.NORMAL);
			
			return;
		}
		
		double dist = _healTarget.GetEntity().getLocation().distanceSquared(GetEntity().getLocation());
		if(dist < HEAL_DISTANCE * HEAL_DISTANCE)
		{
			_healRay = GetEntity().getEyeLocation();
			return;
		}
		
		UtilEnt.CreatureMove(GetEntity(), _healTarget.GetEntity().getLocation(), SPEED_IDLE);
	}
	
	public void movePanic()
	{
		if(_panicTarget == null || UtilEnt.canEntityWalkCloserToNavigationTarget(GetEntity()))
		{
			Vec3D v = RandomPositionGenerator.a(((CraftCreature)GetEntity()).getHandle(), 5, 4);
			_panicTarget = new Location(GetEntity().getWorld(), v.a, v.b, v.c);
		}
		
		UtilEnt.CreatureMove(GetEntity(), _panicTarget, SPEED_PANIC);
	}

}
