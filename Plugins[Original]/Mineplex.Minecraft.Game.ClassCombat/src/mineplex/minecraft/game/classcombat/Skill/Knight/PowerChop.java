package mineplex.minecraft.game.classcombat.Skill.Knight;

import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class PowerChop extends SkillActive
{
	private WeakHashMap<Player, Long> _charge = new WeakHashMap<Player, Long>();

	public PowerChop(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Put more strength into your next",
				"axe attack, causing it to deal",
				"2 bonus damage.",
				"",
				"Attack must be made within",
				"0.5 seconds of being used."
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

		return mineplex.core.recharge.Recharge.Instance.use(player, GetName() + " Cooldown", 250, false, false);
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Action
		_charge.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You prepared " + F.skill(GetName(level)) + "."));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)		return;

		if (!UtilGear.isAxe(damager.getItemInHand()))
			return;

		if (!_charge.containsKey(damager))
			return;
		
		if (UtilTime.elapsed(_charge.remove(damager), 500))
			return;
		
		//Damage
		event.AddMod(damager.getName(), GetName(), 2, true);

		//Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.IRONGOLEM_HIT, 1f, 1f);
	}

	@Override
	public void Reset(Player player) 
	{
		_charge.remove(player);
	}
}
