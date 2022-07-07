package mineplex.minecraft.game.classcombat.Skill.Ranger;
 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class VitalitySpores extends Skill
{
	private HashMap<Player, Long> _lastDamage = new HashMap<>();
	
	public VitalitySpores(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
					"After getting hit, if no damage is taken",
					"for 8 Seconds then you will receive",
					"Regeneration #2#0 for #5#1 Seconds",
				});
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (doesUserHaveSkill(event.GetDamageePlayer()))
		{
			if (event.GetDamageePlayer().hasPotionEffect(PotionEffectType.REGENERATION))
			{
				event.GetDamageePlayer().removePotionEffect(PotionEffectType.REGENERATION);
			}
			else
			{
				_lastDamage.put(event.GetDamageePlayer(), System.currentTimeMillis());
			}
		}
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<Entry<Player, Long>> iterator = _lastDamage.entrySet().iterator();
	    while (iterator.hasNext())
	    {
	    	Entry<Player, Long> entry = iterator.next();
	    	
			if (UtilTime.elapsed(entry.getValue(), 8000))
			{
				iterator.remove();
				entry.getKey().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (20 * (5 + getLevel(entry.getKey()))), 1));
			}
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_lastDamage.remove(player);
	}
}
