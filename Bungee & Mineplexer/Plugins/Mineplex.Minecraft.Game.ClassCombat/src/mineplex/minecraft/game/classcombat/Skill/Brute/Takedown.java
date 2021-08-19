package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Takedown extends SkillActive
{

	private final Map<LivingEntity, Long> _live = new HashMap<>();

	public Takedown(SkillFactory skills, String name, ClassType classType, SkillType skillType,
			int cost, int levels,
			int energy, int energyMod,
			long recharge, long rechargeMod, boolean rechargeInform,
			Material[] itemArray,
			Action[] actionArray)
	{
		super(skills, name, classType, skillType,
				cost, levels,
				energy, energyMod,
				recharge, rechargeMod, rechargeInform,
				itemArray,
				actionArray);

		SetDesc(new String[]
				{
				"Hurl yourself towards an opponent.",
				"If you collide with them, you deal",
				"#5#1 damage and take #1.5#0.5 damage.",
				"You both receive Slow 4 for #2.5#0.5 seconds."
				});
	}

	@Override
	public boolean CustomCheck(Player player, int level)
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		if (UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " while grounded."));
			return false;
		}

		return true;
	}

	@Override
	public void Skill(Player player, int level)
	{
		//Action
		Vector vec = player.getLocation().getDirection();

		UtilAction.velocity(player, vec, 1.2, false, 0, 0.2, 0.4, false);

		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
	}

	@EventHandler
	public void End(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//End
		for (Player player : GetUsers())
		{
			if (!UtilEnt.isGrounded(player))
				continue;

			if (!_live.containsKey(player))
				continue;

			int level = getLevel(player);
			if (level == 0)		continue;

			if (!UtilTime.elapsed(_live.get(player), 1000))
				continue;

			_live.remove(player);
		}
		
		//Collide
		_live.keySet().removeIf(entity ->
		{
			Player player = (Player) entity;

			for (Player other : UtilPlayer.getNearby(player.getLocation(), 2))
			{
				if (player.equals(other) || !Factory.Relation().canHurt(player, other))
				{
					continue;
				}

				DoTakeDown(player, other);
				return true;
			}

			return false;
		});
	}

	private void DoTakeDown(Player damager, LivingEntity damagee)
	{
		int level = getLevel(damager);
		int damage = 5 + level;

		//Damage Event
		CustomDamageEvent customDamageEvent = Factory.Damage().NewDamageEvent(damagee, damager, null,
				DamageCause.CUSTOM, damage, false, true, false,
				damager.getName(), GetName());

		if (!customDamageEvent.IsCancelled())
		{
			//Conditions
			double duration = 2.5 + (level / 2D);

			Factory.Condition().Factory().Slow(GetName(), damagee, damager, duration, 3, false, true, true, true);
			Factory.Condition().Factory().Slow(GetName(), damager, damager, duration, 3, false, true, true, true);
			Factory.Condition().Factory().Jump(GetName(), damagee, damager, duration, 250, false, false, false);
			Factory.Condition().Factory().Jump(GetName(), damager, damager, duration, 250, false, false, false);
			Factory.Damage().NewDamageEvent(damager, damagee, null, DamageCause.CUSTOM, 1.5 + (level / 2D), false, true, false, GetName(), GetName() + " Recoil");

			UtilAction.zeroVelocity(damagee);
			UtilAction.zeroVelocity(damager);

			//Inform
			UtilPlayer.message(damager, F.main(GetClassType().name(), "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName(level)) + "."));
			UtilPlayer.message(damagee, F.main(GetClassType().name(), F.name(damager.getName()) + " hit you with " + F.skill(GetName(level)) + "."));

			//Sound
			damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_WOOD, 1f, 0.5f);
		}
	}

	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Entity ent : _live.keySet())
		{
			UtilParticle.PlayParticleToAll(ParticleType.CRIT, ent.getLocation(),
					(float)(Math.random() - 0.5), (float)(Math.random() * 1.4), (float)(Math.random() - 0.5), 0, 4,
					ViewDist.LONG);
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_live.remove(player);
	}
}
