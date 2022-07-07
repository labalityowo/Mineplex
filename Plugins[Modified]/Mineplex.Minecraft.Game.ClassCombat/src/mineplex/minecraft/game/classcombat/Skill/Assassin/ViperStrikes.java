package mineplex.minecraft.game.classcombat.Skill.Assassin;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilGear;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ViperStrikes extends Skill
{
	public ViperStrikes(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Your attacks give",
				"enemies Poison 1",
				"for #2#2 seconds."
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
		
		if (!UtilGear.isWeapon(damager.getItemInHand()))
			return;

		int level = getLevel(damager);
		if (level == 0)		return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;
		
		Factory.Damage().NewDamageEvent(damagee, damager, null, DamageCause.CUSTOM, 0, false, true, true, damager.getName(), GetName());
		damagee.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * (2 + (2 * level)), 0));
		
		//Sound
		damager.getWorld().playSound(damager.getLocation(), Sound.SPIDER_IDLE, 1f, 2f);
	}

	@Override
	public void Reset(Player player) 
	{

	}
}