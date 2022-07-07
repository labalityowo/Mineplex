package nautilus.game.arcade.game.games.halloween.creatures;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

import org.bukkit.Location;
import org.bukkit.entity.CaveSpider;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class MobSpiderLeaper extends CreatureBase<CaveSpider> implements InterfaceMove
{
	public MobSpiderLeaper(Halloween game, Location loc) 
	{
		super(game, null, CaveSpider.class, loc);
	}

	@Override
	public void SpawnCustom(CaveSpider ent) 
	{
		ent.setCustomName("Leaping Spider");
		
		Host.Manager.GetCondition().Factory().Speed("Speed", GetEntity(), GetEntity(), 99999, 1, false, false, false);
	}
	
	@Override
	public void Damage(CustomDamageEvent event) 
	{
		if (event.GetCause() == DamageCause.FALL)
			event.SetCancelled("Fall Immunity");
	}
	
	@Override
	public void Target(EntityTargetEvent event)
	{
		
	}

	@Override
	public void Update(UpdateEvent event) 
	{
		if (event.getType() == UpdateType.SEC)
			Leap();
	}

	private void Leap() 
	{
		if (GetTarget() == null)
			return;
		
		if (Math.random() > 0.5)
			return;
		
		if (!UtilEnt.isGrounded(GetEntity()))
			return;
		
		if (GetEntity().getTarget() != null)
			UtilAction.velocity(GetEntity(), UtilAlg.getTrajectory2d(GetEntity(), GetEntity().getTarget()), 1, true, 0.6, 0, 10, true);
		else
			UtilAction.velocity(GetEntity(), UtilAlg.getTrajectory2d(GetEntity().getLocation(), GetTarget()), 1, true, 0.6, 0, 10, true);
	}

	public void Move()
	{
		CreatureMove(GetEntity());
	}
}
