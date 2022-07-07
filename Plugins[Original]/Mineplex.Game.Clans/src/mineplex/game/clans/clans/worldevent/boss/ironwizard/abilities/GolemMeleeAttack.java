package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.EntityIronGolem;

public class GolemMeleeAttack extends BossAbility<GolemCreature, IronGolem>
{
	private boolean _attacked;

	public GolemMeleeAttack(GolemCreature creature)
	{
		super(creature);
	}

	@Override
	public boolean canMove()
	{
		return true;
	}

	@Override
	public int getCooldown()
	{
		return 20;
	}

	@Override
	public Player getTarget()
	{
		return getTarget(4);
	}

	@Override
	public boolean hasFinished()
	{
		return _attacked;
	}

	@Override
	public void setFinished() {}

	@Override
	public void tick()
	{
		_attacked = true;

		for (Player target : UtilPlayer.getNearby(getLocation(), 4, true))
		{
			if (target.getVelocity().length() > 0.5)
			{
				continue;
			}

			UtilEnt.CreatureLook(getEntity(), target);

			getBoss().getEvent().getDamageManager().NewDamageEvent(target, getEntity(), null, DamageCause.ENTITY_ATTACK,
					10 * getBoss().getDifficulty(), false, true, false, "Iron Wizard Melee Attack", "Iron Wizard Melee Attack");

			Vector vec = getLocation().getDirection();
			vec.setY(0).normalize().setY(0.5).multiply(2.4);

			UtilAction.velocity(target, vec);

			getBoss().getEvent().getCondition().Factory().Falling("Iron Wizard Throw", target, getEntity(), 3, false, false);

			target.getWorld().playSound(target.getLocation(), Sound.IRONGOLEM_THROW, 3, 0.9F);

			EntityIronGolem golem = ((CraftIronGolem) getEntity()).getHandle();

			golem.world.broadcastEntityEffect(golem, (byte) 4);
		}
	}

	@Override
	public boolean inProgress()
	{
		return _attacked;
	}
}