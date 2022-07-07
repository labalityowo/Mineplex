package mineplex.minecraft.game.classcombat.Skill.Mage;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class NullBlade extends Skill
{
	public NullBlade(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Your attacks suck the life from",
				"opponents, restoring #4#2 energy."
				});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Drain(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		//Damager
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		int level = getLevel(damager);
		if (level == 0)			return;

		//Damagee
		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		//Energy
		Factory.Energy().ModifyEnergy(damager, 4 + 2 * level);

		//Damage
		event.AddMod(damager.getName(), GetName(), 0, false);

		//Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.BLAZE_BREATH, 0.6f, 0.6f);
	}

	@Override
	public void Reset(Player player) 
	{

	}
}
