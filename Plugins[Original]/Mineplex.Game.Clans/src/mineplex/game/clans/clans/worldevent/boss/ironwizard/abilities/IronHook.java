package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import org.bukkit.block.Block;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;

public class IronHook implements IThrown
{
	private WorldEvent _host;
	
	public IronHook(WorldEvent event)
	{
		_host = event;
	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		data.getThrown().remove();

		if (!(data.getThrower() instanceof IronGolem))
			return;

		IronGolem wizard = (IronGolem)data.getThrower();

		if (target == null)
			return;

		UtilAction.velocity(target, UtilAlg.getTrajectory(target.getLocation(), wizard.getLocation()), 5, false, 0, 0.7, 1.2, true);
		
		_host.getCondition().Factory().Falling("Iron Hook", target, wizard, 10, false, true);

		_host.getDamageManager().NewDamageEvent(target, wizard, null, DamageCause.CUSTOM, 5, false, true, false, wizard.getName(), "Iron Hook");
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}