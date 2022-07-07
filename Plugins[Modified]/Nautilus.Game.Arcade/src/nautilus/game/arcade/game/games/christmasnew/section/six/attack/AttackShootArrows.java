package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilTime;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;

import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public class AttackShootArrows extends BossAttack implements IThrown
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(4);

	private BukkitTask _task;

	public AttackShootArrows(BossPhase phase)
	{
		super(phase);
	}

	@Override
	public boolean isComplete()
	{
		return UtilTime.elapsed(_start, DURATION);
	}

	@Override
	public void onRegister()
	{
		Location location = _boss.getLocation().add(0, 2.1, 0);

		_task = _phase.getHost().getArcadeManager().runSyncTimer(() ->
		{
			Arrow arrow = _boss.getWorld().spawn(location, Arrow.class);
			arrow.setCritical(true);
			arrow.setVelocity(new Vector((Math.random() - 0.5) / 1.5D, (Math.random() / 3) + 1, (Math.random() - 0.5) / 1.5D));

			_phase.getHost().getArcadeManager().GetProjectile().AddThrow(arrow, _boss, this, -1, true, true, false, false, 3);
		}, 1, 1);
	}

	@Override
	public void onUnregister()
	{
		_task.cancel();
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target != null)
		{
			String source = data.getThrown().getName();
			_phase.getHost().getArcadeManager().GetDamage().NewDamageEvent(target, _boss, (Projectile) data.getThrown(), DamageCause.CUSTOM, 4, true, true, true, _boss.getCustomName(), source);
		}
	}

	@Override
	public void Idle(ProjectileUser data)
	{

	}

	@Override
	public void Expire(ProjectileUser data)
	{

	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}
