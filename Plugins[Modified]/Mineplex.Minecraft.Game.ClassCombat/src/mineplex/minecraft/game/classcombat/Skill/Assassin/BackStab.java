package mineplex.minecraft.game.classcombat.Skill.Assassin;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class BackStab extends Skill
{
	public BackStab(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Attacks from behind opponents",
				"deal #1.5#1.5 additional damage.",
				});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		int level = getLevel(damager);
		if (level == 0)		return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		Vector look = damagee.getLocation().getDirection();
		look.setY(0);
		look.normalize();

		Vector from = damager.getLocation().toVector().subtract(damagee.getLocation().toVector());
		from.setY(0);
		from.normalize();

		Vector check = new Vector(look.getX() * -1, 0, look.getZ() * -1);
		if (check.subtract(from).length() < 0.8)
		{
			//Damage
			event.AddMod(damager.getName(), GetName(), 1.5 + 1.5 * level, true);
			
			//Effect
			damagee.getWorld().playSound(damagee.getLocation(), Sound.HURT_FLESH, 1f, 2f);	
			damagee.getWorld().playEffect(damagee.getLocation(), Effect.STEP_SOUND, 55);
		}
	}

	@Override
	public void Reset(Player player) 
	{

	}
}