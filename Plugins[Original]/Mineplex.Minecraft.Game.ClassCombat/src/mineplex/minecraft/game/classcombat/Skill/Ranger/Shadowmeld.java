package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilMath;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.classcombat.Skill.SkillChargeSword;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Shadowmeld extends SkillChargeSword
{
	private HashSet<Player> _active = new HashSet<Player>();

	public Shadowmeld(SkillFactory skills, String name, 
			ClassType classType, SkillType skillType, 
			int cost, int maxLevel)
	{
		super(skills, name, classType, skillType, cost, maxLevel, 
				0.01f, 0.005f,
				2000, 0, false, false,
				false, false);

		SetDesc(new String[] 
				{
				"Hold Crouch to meld into the shadows.",
				"",
				"Charges #"+(int)(_rateBase*2000)+"#"+(int)(_rateBoost*2000)+" % per Second.",
				"",
				"Shadowmeld ends if you stop crouching,",
				"interact or another player comes within",
				"#12#-3 Blocks of you."
				});
	}


	@Override
	public void DoSkillCustom(Player player, float charge)
	{
		_active.add(player);
	}

	@EventHandler
	public void EndProximity(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.contains(cur)) 	
				continue;

			int level = getLevel(cur);
			if (level == 0)		continue;

			//No Sneak
			if (!cur.isSneaking())
			{
				End(cur);
				continue;
			}
			
			//Proximity Decloak
			for (Player other : cur.getWorld().getEntitiesByClass(Player.class))
			{
				if (other.equals(cur))
					continue;

				if (UtilMath.offset(cur, other) > 16 - (3 * level))
					continue;

				End(cur);
				continue;
			}

			Factory.Condition().Factory().Cloak(GetName(), cur, cur, 1.9, false, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void EndDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		End(damagee);
	}

	@EventHandler
	public void EndInteract(PlayerInteractEvent event)
	{
		End(event.getPlayer());
	}
	
	@EventHandler
	public void EndBow(EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
			End((Player)event.getEntity());
	}

	public void End(Player player)
	{
		if (_active.remove(player))
			Factory.Condition().EndCondition(player, ConditionType.CLOAK, GetName());
	}

	@Override
	public void Reset(Player player) 
	{
		_charge.remove(player);
		End(player);
	}
}
