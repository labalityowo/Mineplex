package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public class AttackThrowTNT extends BossAttack
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(4);
	private static final int FUSE_TICKS = 50;

	private final int _max;

	public AttackThrowTNT(BossPhase phase, int max)
	{
		super(phase);

		_max = max;
	}

	@Override
	public boolean isComplete()
	{
		return UtilTime.elapsed(_start, DURATION);
	}

	@Override
	public void onRegister()
	{
		Location location = _boss.getEyeLocation();

		for (int i = 0; i < _max; i++)
		{
			TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
			tnt.setFuseTicks(FUSE_TICKS + UtilMath.r(20));

			Vector direction = location.getDirection().multiply(0.3).add(new Vector((Math.random() - 0.5) / 1.5, Math.random() / 3, (Math.random() - 0.5) / 1.5));
			direction.setY(0.8);

			UtilAction.velocity(tnt, direction);
		}
	}

	@Override
	public void onUnregister()
	{

	}

	@EventHandler
	public void explode(EntityExplodeEvent event)
	{
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, event.getLocation(), null, 0, 1, ViewDist.LONG);
	}
}
