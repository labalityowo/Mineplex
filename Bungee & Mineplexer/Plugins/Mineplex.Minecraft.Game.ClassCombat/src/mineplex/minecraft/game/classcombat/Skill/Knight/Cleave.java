package mineplex.minecraft.game.classcombat.Skill.Knight;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Cleave extends Skill
{
	public Cleave(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Your attacks deal #25#25 % damage to",
				"all enemies within 3 Blocks",
				"of your target enemy.",
				"",
				"This only works with Axes."
				});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Skill(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (event.GetReason() != null)
			return;
		
		//Damager
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (!UtilGear.isAxe(damager.getItemInHand()))
			return;

		int level = getLevel(damager);
		if (level == 0)		return;

		//Damagee
		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		//Damage
		event.AddMod(damager.getName(), GetName(), 0, false);

		//Splash
		for (Player other : UtilPlayer.getNearby(damagee.getLocation(), 3))
		{
			if (!other.equals(damagee) && !other.equals(damager))
				if (Factory.Relation().canHurt(damager, other))
				{
					//Damage Event
					Factory.Damage().NewDamageEvent(other, damager, null, 
							DamageCause.ENTITY_ATTACK, (0.25 + (level * 0.25)) * event.GetDamageInitial(), true, false, false,
							damager.getName(), GetName());
				}
		}
	}

	@Override
	public void Reset(Player player)
	{

	}
}
