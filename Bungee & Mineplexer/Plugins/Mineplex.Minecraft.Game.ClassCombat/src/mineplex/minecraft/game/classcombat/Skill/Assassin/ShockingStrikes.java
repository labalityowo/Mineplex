package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilGear;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ShockingStrikes extends Skill
{

	private final Map<Player, ComboData> _combos = new WeakHashMap<>();

	public ShockingStrikes(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Your attacks shock targets for", 
				"#0#1 seconds, giving them:",
				"Slow 1: Every third hit",
				"Blindness: Every #6#-1 seconds",
				"Screen-Shake: Every time",
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

		ComboData data = _combos.get(damager);

		if (data == null || data.Amount > 0 && !damagee.equals(data.Damagee))
		{
			data = new ComboData(damagee);
			_combos.put(damager, data);
		}

		data.Amount++;

		Factory.Condition().Factory().Shock(GetName(), damagee, damager, level, false, false);

		if (data.Amount % 3 == 0)
		{
			Factory.Condition().Factory().Slow(GetName(), damagee, damager, level, 0, false, false, true, false);
		}

		//Damage
		event.AddMod(damager.getName(), GetName(), 0, true);

		String name = "Blinding Strike";

		if (Recharge.Instance.use(damager, name, name + " " + level, 6000 + (1000 * level), false, false))
		{
			Factory.Condition().Factory().Blind(name, damagee, damager, level, 1, true, true, false);
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_combos.remove(player);
	}

	private class ComboData
	{

		final LivingEntity Damagee;
		int Amount;

		ComboData(LivingEntity damagee)
		{
			Damagee = damagee;
		}
	}
}