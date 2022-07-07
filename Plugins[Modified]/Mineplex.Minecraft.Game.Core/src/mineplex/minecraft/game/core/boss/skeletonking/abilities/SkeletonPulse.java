package mineplex.minecraft.game.core.boss.skeletonking.abilities;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.skeletonking.SkeletonCreature;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

public class SkeletonPulse extends BossAbility<SkeletonCreature, Skeleton>
{
	private static final long TOTAL_ATTACK_DURATION = 8000;
	private static final long TOTAL_ATTACK_PROGRESS = 3000;
	private long _start, _lastIncrement;
	private int _radius;
	private Location _center;
	
	public SkeletonPulse(SkeletonCreature creature)
	{
		super(creature);
		_start = System.currentTimeMillis();
		_radius = 2;
		_lastIncrement = System.currentTimeMillis();
		_center = creature.getEntity().getLocation();
	}
	
	private int getRadius()
	{
		return Math.min(8, _radius);
	}
	
	@Override
	public int getCooldown()
	{
		return 7;
	}

	@Override
	public boolean canMove()
	{
		return UtilTime.elapsed(_start, TOTAL_ATTACK_PROGRESS);
	}

	@Override
	public boolean inProgress()
	{
		return UtilTime.elapsed(_start, TOTAL_ATTACK_PROGRESS);
	}

	@Override
	public boolean hasFinished()
	{
		return UtilTime.elapsed(_start, TOTAL_ATTACK_DURATION);
	}

	@Override
	public void setFinished()
	{
		_start = System.currentTimeMillis() - TOTAL_ATTACK_DURATION;
	}

	@Override
	public void tick()
	{
		if (UtilTime.elapsed(_lastIncrement, 500))
		{
			_lastIncrement = System.currentTimeMillis();
			_radius++;
		}

		for (double token = 0; token <= (2 * Math.PI); token += .2)
		{
			double x = getRadius() * Math.cos(token);
			double z = getRadius() * Math.sin(token);
			
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, _center.clone().add(x, 1.5, z), null, 0, 1, ViewDist.MAX);
		}

		for (Player player : UtilPlayer.getInRadius(getEntity().getLocation(), getRadius()).keySet())
		{
			if (player.isDead() || !player.isValid() || !player.isOnline())
			{
				continue;
			}
			if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
			{
				continue;
			}
			if (!Recharge.Instance.use(player, "Pulse Knockback", 400, false, false, false))
			{
				continue;
			}
			
			player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1f, 1f);
			UtilAction.velocity(player, UtilAlg.getTrajectory2d(getEntity(), player), 2, false, 0.6, 0, 1.4, true);
		}
	}
}