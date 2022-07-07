package mineplex.minecraft.game.core.boss.slimeking.ability;

import java.util.Iterator;
import java.util.LinkedList;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.boss.slimeking.creature.SlimeCreature;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class RocketAbility extends SlimeAbility implements IThrown
{
	private int _rocketCount;
	private int _rocketsFired;
	private int _rocketsHit;
	private LinkedList<ShotData> _shots;

	public RocketAbility(SlimeCreature slime)
	{
		this(slime, 5);
	}

	public RocketAbility(SlimeCreature slime, int rocketCount)
	{
		super(slime);
		_rocketCount = rocketCount;
		_rocketsFired = 0;
		_rocketsHit = 0;
		_shots = new LinkedList<ShotData>();
	}

	@Override
	public void tickCustom()
	{
		if (_rocketsHit >= _rocketCount)
		{
			// We're done here!
			setIdle(true);
			return;
		}

		if (_rocketsFired < _rocketCount && getTicks() % 20 == 0)
		{
			Player target = UtilPlayer.getRandomTarget(getSlime().getEntity().getLocation(), 20);

			if (target == null && getTicks() > 20 * (_rocketCount + 10))
			{
				// Give up on firing more rockets
				_rocketCount = _rocketsFired;
			}

			if (target != null) fireRocket(target);
		}

		tickRockets();
	}

	private void tickRockets()
	{
		Iterator<ShotData> it = _shots.iterator();

		while (it.hasNext())
		{
			ShotData next = it.next();

			if (next.getEntity().isDead())
			{
				it.remove();
			}
			else
			{
				Vector v = UtilAlg.getTrajectory(next.getEntity(), next.getTarget());
				next.getEntity().setVelocity(v.multiply(new Vector(0.3, 0.1, 0.3)));
			}

		}
	}

	private void fireRocket(Player target)
	{
		Location loc = getSlime().getEntity().getEyeLocation();
		loc.add(UtilAlg.getTrajectory2d(loc, target.getLocation()).multiply(2));
		Slime projectile = loc.getWorld().spawn(loc, Slime.class);
		projectile.setSize(1);
		_shots.add(new ShotData(projectile, target));

		ProjectileManager pm = getSlime().getProjectileManager();
		pm.AddThrow(projectile, getSlime().getEntity(), this, -1, true, true, true, false, null, 0, 0, UtilParticle.ParticleType.SLIME, UpdateType.FASTEST, 1F);
//		Bukkit.broadcastMessage("Shot Slime at target " + target);

		_rocketsFired++;
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
//		Bukkit.broadcastMessage("COLLIDE " + target);
		UtilParticle.PlayParticle(UtilParticle.ParticleType.LARGE_EXPLODE, data.getThrown().getLocation(), 0, 0, 0, 0, 1, UtilParticle.ViewDist.LONG, UtilServer.getPlayers());
		target.getLocation().getWorld().playSound(target.getLocation(), Sound.SPLASH, 1, 2);
		getSlime().getEvent().getDamageManager().NewDamageEvent(target, getSlime().getEntity(), null,
				EntityDamageEvent.DamageCause.PROJECTILE, 3 + getSlime().getSize() * 3, true, true, false,
				getSlime().getName(), "Slime Rocket");

		data.getThrown().remove();
		_rocketsHit++;
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.getThrown().remove();
		_rocketsHit++;
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.getThrown().remove();
		_rocketsHit++;
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	private static class ShotData
	{
		private LivingEntity _entity;
		private LivingEntity _target;

		public ShotData(LivingEntity entity, LivingEntity target)
		{
			_entity = entity;
			_target = target;
		}

		public LivingEntity getEntity()
		{
			return _entity;
		}

		public LivingEntity getTarget()
		{
			return _target;
		}
	}
}