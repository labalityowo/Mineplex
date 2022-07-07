package mineplex.game.clans.clans.worldevent.boss.skeletonking.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSkeleton;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonCreature;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.UndeadArcherCreature;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SkeletonArcherShield extends BossAbility<SkeletonCreature, Skeleton>
{
	private static long ATTACK_DURATION = 10000;
	private static long ARROW_DELAY = 100;
	private static long ARROW_WARMUP = 2000;
	private static double PULL_RANGE = 12;
	private long _start;
	private long _lastShoot;
	private boolean _teleported;
	
	public SkeletonArcherShield(SkeletonCreature creature)
	{
		super(creature);
		_start = System.currentTimeMillis();
	}
	
	@Override
	public int getCooldown()
	{
		return 20;
	}

	@Override
	public boolean canMove()
	{
		return false;
	}

	@Override
	public boolean inProgress()
	{
		return false;
	}

	@Override
	public boolean hasFinished()
	{
		return UtilTime.elapsed(_start, ATTACK_DURATION);
	}

	@Override
	public void setFinished()
	{
		_start = System.currentTimeMillis() - ATTACK_DURATION;
		for (UndeadArcherCreature creature : getBoss().Archers)
		{
			creature.getEntity().remove();
		}
	}
	
	private void run(boolean initial)
	{
		for (int i = 0; i < getBoss().Archers.size(); i++)
		{
			Skeleton archer = getBoss().Archers.get(i).getEntity();
			UtilEnt.vegetate(archer);
			((CraftSkeleton)archer).setVegetated(false);

			double lead =  i * ((2d * Math.PI)/getBoss().Archers.size());

			double sizeMod = 2;

			//Orbit
			double speed = 10d;
			double oX = -Math.sin(getEntity().getTicksLived()/speed + lead) * 2 * sizeMod;
			double oY = 0;
			double oZ = Math.cos(getEntity().getTicksLived()/speed + lead) * 2 * sizeMod;

			if (initial)
			{
				archer.teleport(getEntity().getLocation().add(oX, oY, oZ));
				UtilEnt.vegetate(archer);
			}
			else
			{
				Location to = getEntity().getLocation().add(oX, oY, oZ);
				UtilEnt.LookAt(archer, to);
				UtilAction.velocity(archer, UtilAlg.getTrajectory(archer.getLocation(), to), 0.4, false, 0, 0.1, 1, true);
			}
		}
	}
	
	private void shoot()
	{
		if (UtilTime.elapsed(_start, ARROW_WARMUP) && UtilTime.elapsed(_lastShoot, ARROW_DELAY))
		{
			_lastShoot = System.currentTimeMillis();
			for (UndeadArcherCreature archer : getBoss().Archers)
			{
				Location spawn = archer.getEntity().getEyeLocation().add(UtilAlg.getTrajectory(getEntity().getEyeLocation(), archer.getEntity().getEyeLocation()).normalize());
				Vector vector = UtilAlg.getTrajectory(getEntity().getEyeLocation(), spawn);
				Arrow arrow = archer.getEntity().getWorld().spawnArrow(spawn, vector, 0.6f, 12f);
				arrow.setMetadata("SHIELD_SHOT", new FixedMetadataValue(UtilServer.getPlugin(), true));
				arrow.setMetadata("BARBED_ARROW", new FixedMetadataValue(UtilServer.getPlugin(), 10));
				arrow.setShooter(archer.getEntity());
			}
		}
	}

	@Override
	public void tick()
	{
		if (!_teleported)
		{
			run(true);
			_teleported = true;
			for (Player near : UtilPlayer.getInRadius(getEntity().getLocation(), PULL_RANGE).keySet())
			{
				Vector velocity = UtilAlg.getTrajectory(near, getEntity());
				UtilAction.velocity(near, velocity, 2, false, 0, 0, 1, true);
				near.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5 * 20, -2));
		        for (int i = 0; i < 6; i++)
		        {
					Vector random = new Vector(Math.random() * 4 - 2, Math.random() * 4 - 2, Math.random() * 4 - 2);

					Location origin = getEntity().getLocation().add(0, 1.3, 0);
					origin.add(velocity.clone().multiply(10));
					origin.add(random);

					Vector vel = UtilAlg.getTrajectory(origin, getEntity().getLocation().add(0, 1.3, 0));
					vel.multiply(7);

					UtilParticle.PlayParticleToAll(ParticleType.MAGIC_CRIT, origin, (float)vel.getX(), (float)vel.getY(), (float)vel.getZ(), 1, 0, ViewDist.LONG);
		        }
			}
		}
		else
		{
			run(false);
			shoot();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(getBoss().getEntity()))
		{
			if (!hasFinished())
			{
				event.SetCancelled("Wraiths Alive");
			}
		}
	}
	
	@EventHandler
	public void onArrowHit(ProjectileHitEvent event)
	{
		if (event.getEntity().hasMetadata("SHIELD_SHOT"))
		{
			Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), () ->
			{
				event.getEntity().remove();
			}, 20L);
		}
	}
}