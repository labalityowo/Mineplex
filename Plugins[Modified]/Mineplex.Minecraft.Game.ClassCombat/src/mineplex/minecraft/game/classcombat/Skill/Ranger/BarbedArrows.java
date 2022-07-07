package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class BarbedArrows extends Skill
{

	private final Set<Entity> _arrows = new HashSet<>();

	public BarbedArrows(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Your arrows are barbed, and give", 
				"opponents Slow 1 for #2#1 seconds.",
				"Will cancel sprint on opponents.",
				"",
				"Duration scales with arrow velocity."
				});
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void bowShoot(EntityShootBowEvent event)
	{
		int level = getLevel(event.getEntity());

		if (level == 0)
		{
			return;
		}

		_arrows.add(event.getProjectile());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Projectile projectile = event.GetProjectile();

		if (projectile == null || !_arrows.remove(projectile))
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(true);

		if (damagee == null || damager == null)
		{
			return;
		}

		//Level
		int level = getLevel(damager);
		if (level == 0)
		{
			return;
		}

		//Condition
		int duration = 3 + level;

		Factory.Condition().Factory().Slow(GetName(), damagee, damager, duration, 0, false, true, true, true);
		Factory.Condition().Factory().Regen(GetName(), damagee, damager, duration, 0, false, false, false);
		Factory.Condition().Factory().ArcadeHungerDisable(GetName(), damagee, damager, duration, false);

		Player damageePlayer = event.GetDamageePlayer();

		if (damageePlayer != null)
		{
			int food = damageePlayer.getFoodLevel();

			damageePlayer.setFoodLevel(2);
			damageePlayer.setSprinting(false);

			Factory.runSyncLater(() -> damageePlayer.setFoodLevel(food), duration * 20);
		}
	}

	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		_arrows.removeIf(arrow -> !arrow.isValid());
	}

	@Override
	public void Reset(Player player) 
	{

	}
}
