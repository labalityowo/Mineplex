package nautilus.game.arcade.game.games.halloween.creatures;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.HalloweenAudio;
import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.NavigationAbstract;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PumpkinKing extends CreatureBase<Skeleton>
{
	private int _state = 0;
	private long _stateTime = System.currentTimeMillis();

	private ArrayList<Skeleton> _minions = new ArrayList<Skeleton>();
	private int _minionsMax = 12; //12
	private boolean _minionSpawn = true;
	private HashMap<Entity, Player> _minionTargets = new HashMap<Entity, Player>();
	private HashMap<Skeleton, Long> _minionAttack = new HashMap<Skeleton, Long>();

	private ArrayList<Entity> _shields = new ArrayList<Entity>();
	private int _shieldsMax = 6; //6
	private long _shieldSpawn = 0;

	private Location _kingLocation;
	private Player _kingTarget = null;

	private HashSet<Arrow> _arrows = new HashSet<Arrow>();

	private boolean _announcedHalfHealth = false;

	public PumpkinKing(Halloween game, Location loc) 
	{
		super(game, null, Skeleton.class, loc);

		_kingLocation = loc;

		game.bossDebug = true;
	}

	@Override
	public void SpawnCustom(Skeleton ent) 
	{
		ent.setSkeletonType(SkeletonType.WITHER);
		ent.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));

		ent.setMaxHealth(400);
		ent.setHealth(ent.getMaxHealth());

		ent.getWorld().strikeLightningEffect(ent.getLocation());
		ent.getWorld().strikeLightningEffect(ent.getLocation());
		ent.getWorld().strikeLightningEffect(ent.getLocation());
	}

	@Override
	public void Damage(CustomDamageEvent event) 
	{
		//Attacked King
		if (event.GetDamageeEntity().equals(GetEntity()))
		{
			if (event.GetProjectile() != null)
				event.GetProjectile().remove();

			if (_shields.size() > 0)
			{
				event.SetCancelled("Shielded");
				UtilPlayer.message(event.GetDamagerPlayer(true), F.main("Boss", "You must destroy " + F.elem("Flame Shields") + " first!"));
			}
			else if (_minions.size() > 0)
			{
				event.SetCancelled("Shielded");
				UtilPlayer.message(event.GetDamagerPlayer(true), F.main("Boss", "You must destroy " + F.elem("Pumpkin Minions") + " first!"));
			}

			if (event.GetDamagerPlayer(true) == null)
				event.SetCancelled("Non-Player");
			
			if (event.GetDamagerPlayer(true) != null)
				event.GetDamagerPlayer(true).setFoodLevel(20);

			event.SetKnockback(false);
		}
		//Attacked Minion
		else if (_minions.contains(event.GetDamageeEntity()))
		{
			if (event.GetProjectile() != null)
				event.GetProjectile().remove();

			if (_shields.size() > 0)
			{
				event.SetCancelled("Shielded");
				UtilPlayer.message(event.GetDamagerPlayer(true), F.main("Boss", "You must destroy " + F.elem("Flame Shields") + " first!"));
			}
			else if (event.GetProjectile() != null)
			{
				event.SetCancelled("Projectile");
				UtilPlayer.message(event.GetDamagerPlayer(true), F.main("Boss", "Projectiles cannot harm " + F.elem("Pumpkin Minions") + "!"));
			}
			else
			{
				if (event.GetDamagerPlayer(true) != null)
					event.GetDamagerPlayer(true).setFoodLevel(20);
			}
		}
		//Attacked Shield
		else if (_shields.contains(event.GetDamageeEntity()))
		{
			event.SetCancelled("Shield Break");

			if (event.GetCause() != DamageCause.PROJECTILE && event.GetCause() != DamageCause.LIGHTNING)
				return;

			event.GetProjectile().remove();

			if (event.GetDamagerPlayer(true) == null)
				return;
			
			if (event.GetDamagerPlayer(true) != null)
				event.GetDamagerPlayer(true).setFoodLevel(20);

			//Effect
			Host.Manager.GetBlood().Effects(null, event.GetDamageeEntity().getLocation(), 10, 0.2, null, 0f, 0f, Material.BLAZE_POWDER, (byte)0, 10, false);
			event.GetDamageeEntity().getWorld().playEffect(event.GetDamageeEntity().getLocation(), Effect.STEP_SOUND, 51);

			//Remove
			_shields.remove(event.GetDamageeEntity());
			event.GetDamageeEntity().remove();

			//Health
			KingUpdateHealth();

			//Shield Spawn Delay
			_shieldSpawn = System.currentTimeMillis();
		}
	}

	@Override
	public void Update(UpdateEvent event) 
	{
		long start = System.currentTimeMillis();

		//Main
		if (event.getType() == UpdateType.FASTER)
			StateUpdate();
		Host.updateBossA += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.FAST)
			KingDestroyBlocks();
		Host.updateBossB += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.TICK)
			KingUpdateHealth();
		Host.updateBossC += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();

		//Minions
		if (event.getType() == UpdateType.TICK)
			MinionOrbit();
		Host.updateBossD += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.FASTER)
			MinionMove();
		Host.updateBossE += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.TICK)
			MinionAttackDamage();
		Host.updateBossF += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.FASTEST)
			MinionArrow();
		Host.updateBossG += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.FAST)
			MinionSpawn();
		Host.updateBossH += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();

		//Final
		if (event.getType() == UpdateType.FAST)
			KingControl();
		Host.updateBossI += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.SEC)
			KingLeap();
		Host.updateBossJ += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.SEC)
			KingBomb();
		Host.updateBossK += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.SEC)
			KingTarget();
		Host.updateBossL += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.TICK)
			KingTrail();
		Host.updateBossM += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		//Shield
		if (event.getType() == UpdateType.TICK)
			ShieldOrbit(false);
		Host.updateBossN += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		if (event.getType() == UpdateType.FAST)
			ShieldSpawn();
		Host.updateBossO += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
	}



	private void KingTrail()
	{
		if (GetState() >= 4)
		{
			//Particles
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, GetEntity().getLocation().add(0, 1.5, 0), 0.2f, 0.4f, 0.2f, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
		}
	}

	private void KingTarget() 
	{
		if (Math.random() > 0.80)
		{
			_kingTarget = GetRandomPlayer();
			KingLeap();
		}
	}

	private void KingControl() 
	{
		if (GetState() >= 4)
		{
			if (_kingTarget == null)
				_kingTarget = GetRandomPlayer();

			GetEntity().setTarget(_kingTarget);

			Location loc = _kingTarget.getLocation();
			if (UtilMath.offset(loc, GetEntity().getLocation()) > 16)
				loc = GetEntity().getLocation().add(UtilAlg.getTrajectory(GetEntity().getLocation(), loc).multiply(16));

			//Move
			EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
			NavigationAbstract nav = ec.getNavigation();
			nav.a(loc.getX(), loc.getY(), loc.getZ(), 1f);
		}
		else
		{
			GetEntity().teleport(_kingLocation);
		}
	}

	private void KingLeap() 
	{
		if (GetState() < 4)
			return;

		if (_kingTarget == null)
			return;

		if (Math.random() > 0.4)
			return;

		UtilAction.velocity(GetEntity(), UtilAlg.getTrajectory2d(GetEntity(), _kingTarget), 1.2, false, 0, 0.4, 10, true);
	}

	private void KingBomb() 
	{
		if (GetState() < 4)
			return;

		if (_kingTarget == null)
			return;

		if (Math.random() > 0.4)
			return;

		TNTPrimed tnt = GetEntity().getWorld().spawn(GetEntity().getEyeLocation().add(GetEntity().getLocation().getDirection()), TNTPrimed.class);

		Player target = GetRandomPlayer();

		UtilAction.velocity(tnt, UtilAlg.getTrajectory(tnt, target), 1.2, false, 0, 0.4, 10, false);
	}

	private void KingUpdateHealth() 
	{
		String text = "";
		double percent = 0;
		
		if (_shields.size() > 0)
		{
			percent = (double)_shields.size() / (double)_shieldsMax;
			text = C.cGreen + C.Bold + "Kill the Flame Shield";
		}
		else 
		{
			if (_minions.size() > 0)
			{
				percent = (double)_minions.size() / (double)_minionsMax;
				text = C.cYellow + C.Bold + "Kill the Pumpkin Soldiers";
			}
			else
			{
				percent = GetEntity().getHealth()/GetEntity().getMaxHealth();
				text = C.cGold + C.Bold + "Kill the Pumpkin King";
			}
		}

		UtilTextTop.displayProgress(text, percent, UtilServer.getPlayers());
	}

	private void KingDestroyBlocks() 
	{
		Host.Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(GetEntity().getLocation(), 7d).keySet(), GetEntity().getLocation(), true);
	} 

	@EventHandler
	public void MinionSpawn()
	{	
		Iterator<Skeleton> shieldIterator = _minions.iterator();
		while (shieldIterator.hasNext())
		{
			Skeleton skel = shieldIterator.next();

			if (!skel.isValid())
				shieldIterator.remove();
		}

		if (!_minionSpawn)
			return;

		for (int i=0 ; i<_minionsMax ; i++)
		{
			Host.CreatureAllowOverride = true;
			Skeleton skel = GetEntity().getWorld().spawn(GetEntity().getLocation(), Skeleton.class);
			Host.CreatureAllowOverride = false;

			Host.Manager.GetCondition().Factory().Invisible("Cloak", skel, skel, 999999, 0, false, false, false);

			skel.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
			skel.getEquipment().setItemInHand(new ItemStack(Material.BOW));

			skel.setMaxHealth(50);
			skel.setHealth(skel.getMaxHealth());

			_minions.add(skel);

			UtilEnt.vegetate(skel);
		}	

		_minionSpawn = false;
	}

	public void MinionOrbit()
	{
		if (GetState() != 0 && GetState() != 1  && GetState() != 2)
			return;

		for (int i=0 ; i<_minions.size() ; i++)
		{
			Skeleton minion = _minions.get(i);

			UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, minion.getEyeLocation(), 0.1f, 0.1f, 0.1f, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());

			minion.setTarget(null);

			double lead =  i * ((2d * Math.PI)/_minions.size());

			double sizeMod = 2 + (_minions.size() / 12);

			//Orbit
			double speed = 20d;
			double oX = Math.sin(GetEntity().getTicksLived()/speed + lead) * 2 * sizeMod;
			double oY = 1;
			double oZ = Math.cos(GetEntity().getTicksLived()/speed + lead) * 2 * sizeMod;
			Location loc = GetEntity().getLocation().add(oX, oY, oZ);

			if (UtilMath.offset(loc, minion.getLocation()) > 16)
			{
				Host.Manager.GetBlood().Effects(null, minion.getEyeLocation(), 10, 0.2, Sound.SKELETON_HURT, 1f, 1f, Material.BONE, (byte)0, 20, false);
				minion.teleport(loc);
				continue;
			}

			loc.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(GetEntity(), minion)));

			//Move
			UtilEnt.CreatureMoveFast(minion, loc, 1.4f);
			//			EntityCreature ec = ((CraftCreature)minion).getHandle();
			//			ec.getControllerMove().a(loc.getX(), loc.getY(), loc.getZ(), 1.4);
		}
	}

	public void MinionMove()
	{
		if (GetState() != 3)
			return;

		if (_minions.isEmpty())
			return;

		Skeleton minion = _minions.remove(0);

		LivingEntity target = _minionTargets.get(minion);
		if (target == null)
			return;

		minion.setTarget(target);

		UtilEnt.CreatureMove(minion, target.getLocation(), 1f);

		_minions.add(minion);
	}

	private void MinionAttackDamage() 
	{
		if (GetState() != 3)
			return;

		for (int i=0 ; i<_minions.size() ; i++)
		{
			final Skeleton minion = _minions.get(i);

			UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, minion.getEyeLocation(), 0.1f, 0.1f, 0.1f, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());

			LivingEntity target = _minionTargets.get(minion);
			if (target == null)
				continue;

			if (UtilMath.offset(minion, target) > 2)
				continue;

			if (_minionAttack.containsKey(minion) && !UtilTime.elapsed(_minionAttack.get(minion), 500))
				continue;

			//Damage Event
			Host.Manager.GetDamage().NewDamageEvent(target, minion, null, 
					DamageCause.ENTITY_ATTACK, 4, true, false, false,
					UtilEnt.getName(minion), GetName());	

			//Sound
			minion.getWorld().playSound(minion.getLocation(), Sound.ENDERMAN_SCREAM, 2f, 2f);
			minion.getWorld().playSound(minion.getLocation(), Sound.ENDERMAN_SCREAM, 2f, 2f);

			//Visual Start
			minion.getEquipment().setHelmet(new ItemStack(Material.JACK_O_LANTERN));

			//Visual End
			UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Host.Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					minion.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
				}
			}, 4);

			_minionAttack.put(minion, System.currentTimeMillis());
		}
	}

	public void MinionArrow()
	{
		//Clean
		Iterator<Arrow> arrowIterator = _arrows.iterator();	
		while (arrowIterator.hasNext())
		{
			Arrow arrow = arrowIterator.next();

			if (arrow.getLocation().getY() > 30 && arrow.getVelocity().getY() > 0)
			{
				Player target = Host.GetPlayers(true).get(UtilMath.r(Host.GetPlayers(true).size()));
				arrow.teleport(target.getLocation().add(Math.random() * 8 - 4, Math.random() * 10 + 30, Math.random() * 8 - 4));
				arrow.setVelocity(arrow.getVelocity().setY(-0.1));
				continue;
			}

			if (arrow.getTicksLived() > 200 || arrow.isOnGround())
			{
				if (arrow.isValid() && Math.random() > 0.50)
				{
					try
					{
						EntityArrow entityArrow = ((CraftArrow)arrow).getHandle();

						Field fieldX = EntityArrow.class.getDeclaredField("d");
						Field fieldY = EntityArrow.class.getDeclaredField("e");
						Field fieldZ = EntityArrow.class.getDeclaredField("f");

						fieldX.setAccessible(true);
						fieldY.setAccessible(true);
						fieldZ.setAccessible(true);

						int x = fieldX.getInt(entityArrow);
						int y = fieldY.getInt(entityArrow);
						int z = fieldZ.getInt(entityArrow);

						Block block = arrow.getWorld().getBlockAt(x, y, z);

						if (block.getY() > GetEntity().getLocation().getY())
							block.breakNaturally();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				arrow.remove();
				arrowIterator.remove();
			}
		}

		//Circle
		if (GetState() == 1)
		{
			for (int i=0 ; i<_minions.size() ; i++)
			{
				Skeleton minion = _minions.get(i);

				if (!minion.isValid())
					continue;

				Vector traj = UtilAlg.getTrajectory2d(GetEntity(), minion);
				traj.add(new Vector(0,Math.random()*0.25,0));

				Arrow arrow = GetEntity().getWorld().spawnArrow(minion.getEyeLocation().add(traj), traj, 2f, 16f);
				arrow.setShooter(minion);

				_arrows.add(arrow);
			}
		}

		//Up
		else if (GetState() == 2)
		{
			for (int i=0 ; i<_minions.size() ; i++)
			{
				Skeleton minion = _minions.get(i);

				if (!minion.isValid())
					continue;

				Vector traj = new Vector(0,1,0);

				Arrow arrow = GetEntity().getWorld().spawnArrow(minion.getEyeLocation().add(traj), traj, 2f, 16f);
				arrow.setShooter(minion);

				_arrows.add(arrow);
			}
		}
	}

	@EventHandler
	public void ShieldSpawn()
	{		
		if (GetState() == 3 || GetState() == 4)
			return;

		Iterator<Entity> shieldIterator = _shields.iterator();
		while (shieldIterator.hasNext())
		{
			Entity ent = shieldIterator.next();

			if (!ent.isValid())
				shieldIterator.remove();
		}

		if (!UtilTime.elapsed(_shieldSpawn, 10000))
			return;

		if (_shields.size() >= _shieldsMax)
			return;

		//Delay
		_shieldSpawn = System.currentTimeMillis();

		int toSpawn = 1;
		if (_shields.size() == 0)
		{
			toSpawn = _shieldsMax;

			//Sound
			GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.WITHER_HURT, 10f, 1.5f);

			if (GetEntity().getTicksLived() > 100)
				Host.Announce(C.cAqua + C.Bold + "Flame Shield has regenerated!");
		}
		else
		{
			//Sound
			GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.WITHER_HURT, 1f, 2f);
		}

		for (int i=0 ; i<toSpawn ; i++)
		{
			//Spawn
			Host.CreatureAllowOverride = true;
			Blaze ent = GetEntity().getWorld().spawn(GetEntity().getLocation().add(0, 6, 0), Blaze.class);
			ent.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
			_shields.add(ent);
			UtilEnt.vegetate(ent);
			//ent.setSize(1);
			Host.CreatureAllowOverride = false;

			//Orbit
			ShieldOrbit(false);
		}
	}

	public void ShieldOrbit(boolean teleport)
	{
		for (int i=0 ; i<_shields.size() ; i++)
		{
			Entity shield = _shields.get(i);

			UtilParticle.PlayParticle(ParticleType.FLAME, shield.getLocation(), 0.1f, 0.1f, 0.1f, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());

			double lead =  i * ((2d * Math.PI)/_shields.size());

			double sizeMod = 2;

			//Orbit
			double speed = 10d;
			double oX = -Math.sin(GetEntity().getTicksLived()/speed + lead) * 2 * sizeMod;
			double oY = 6;
			double oZ = Math.cos(GetEntity().getTicksLived()/speed + lead) * 2 * sizeMod;

			if (teleport)
			{
				shield.teleport(GetEntity().getLocation().add(oX, oY, oZ));
			}
			else
			{
				UtilAction.velocity(shield, 
						UtilAlg.getTrajectory(shield.getLocation(), GetEntity().getLocation().add(oX, oY, oZ)), 
						0.4, false, 0, 0.1, 1, true);
			}
		}

		if (_shields.size() > 0)
			GetEntity().getWorld().playEffect(GetEntity().getLocation().add(0, 6, 0), Effect.ENDER_SIGNAL, 0);
	}

	public int GetState() 
	{
		return _state;
	}

	public void SetState(int state)
	{
		_state = state;
		_stateTime = System.currentTimeMillis();


		if (state == 3)
		{
			//Update Gear
			for (int i=0 ; i<_minions.size() ; i++)
			{
				Skeleton minion = _minions.get(i);

				minion.getEquipment().setItemInHand(null);

				//Speed
				Host.Manager.GetCondition().Factory().Speed("Minion Speed", minion, minion, 15, 0, false, false, false);

				//Sound
				//GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.WITHER_SPAWN, 1f, 1.5f);

				//Target
				_minionTargets.put(minion, GetRandomPlayer());
			}

			//Announce
			Host.Announce(C.cAqua + C.Bold + "Kill the Pumpkin Minions!");

			MinionMove();

			Host.playSound(HalloweenAudio.BOSS_STAGE_MINION_ATTACK);
		}

		if (state == 4)
		{
			Host.playSound(HalloweenAudio.BOSS_STAGE_FINAL);
		}
	}

	public void StateUpdate()
	{
		if (GetEntity() == null)
			return;

		if (GetState() == 0)
		{
			if (UtilTime.elapsed(_stateTime, 10000))
			{
				if (Math.random() > 0.5)
					SetState(1);
				else
					SetState(2);
			}
		}
		//Outward Arrows
		else if (GetState() == 1)
		{
			if (UtilTime.elapsed(_stateTime, 5000))
			{
				SetState(0);
			}
		}
		//Upwards Arrows
		else if (GetState() == 2)
		{
			if (UtilTime.elapsed(_stateTime, 5000))
			{
				SetState(0);
			}
		}
		else if (GetState() == 3)
		{
			if (UtilTime.elapsed(_stateTime, 20000))
			{
				SetState(0);

				//Update Minions
				for (int i=0 ; i<_minions.size() ; i++)
				{
					Skeleton minion = _minions.get(i);
					minion.setTarget(null);
					minion.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
					minion.getEquipment().setItemInHand(new ItemStack(Material.BOW));	
				}

				ShieldSpawn();

				Host.playSound(HalloweenAudio.BOSS_STAGE_SHIELD_RESTORE);

				_minionTargets.clear();
			}
		}
		else if (GetState() == 4)
		{
			if (!_announcedHalfHealth)
			{
				if (GetEntity().getHealth() < GetEntity().getMaxHealth()/2)
				{
					Host.playSound(HalloweenAudio.BOSS_STAGE_FINAL_HALF_DEAD);
					_announcedHalfHealth = true;
				}

			}
		}

		//Skeleton Scatter
		if (GetState() != 3 && UtilTime.elapsed(_stateTime, 2000))
		{
			if (_shields.size() == 0 && _minions.size() > 0)
			{
				SetState(3);
			}
		}

		//Final Stage
		if (GetState() != 4 && UtilTime.elapsed(_stateTime, 2000))
		{
			if (_shields.size() == 0 && _minions.size() == 0)
			{
				SetState(4);

				//Announce
				Host.Announce(C.cAqua + C.Bold + "Kill the Pumpkin King!!!");

				//Sound
				GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.WITHER_SPAWN, 10f, 1.5f);

				//Speed
				Host.Manager.GetCondition().Factory().Speed("King Speed", GetEntity(), GetEntity(), 9999, 1, false, false, false);

				//Equip
				GetEntity().getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD));
			}
		}
	}

	public void Target(EntityTargetEvent event)
	{
		if (event.getEntity().equals(GetEntity()))
		{
			if (GetState() != 4 || _kingTarget == null || !_kingTarget.equals(event.getTarget()))
			{
				event.setCancelled(true);
			}
		}

		if (_minions.contains(event.getEntity()))
		{
			if (GetState() != 3)
			{
				event.setCancelled(true);
			}
			else 
			{
				if (!_minionTargets.containsKey(event.getEntity()))
				{
					event.setCancelled(true);
				}
				else
				{
					Player player = _minionTargets.get(event.getEntity());

					if (!player.equals(event.getTarget()))
						event.setCancelled(true);

					if (!Host.IsAlive(player))
						_minionTargets.put(event.getEntity(), GetRandomPlayer());
				}
			}
		}	
	}

	public Player GetRandomPlayer()
	{
		if (Host.GetPlayers(true).isEmpty())
			return null;

		return Host.GetPlayers(true).get(UtilMath.r(Host.GetPlayers(true).size()));
	}

	public boolean IsFinal() 
	{
		return _minions.size() == 0;
	}
}
