package mineplex.minecraft.game.classcombat.Skill.Assassin;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Leap extends SkillActive
{
	public Leap(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Take a great leap forwards.",
				"",	
				"Wall Kick by using Leap with your",
				"back against a wall. This doesn't",
				"trigger Leaps Recharge but has a cooldown",
				"of 1.5 seconds.",
				"",
				"Cannot be used while Slowed."
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

		if (UtilTime.elapsed(Factory.Movement().Get(player).LastGrounded, 8000))
		{
			UtilPlayer.message(player, F.main(GetClassType().name(), "You cannot use " + F.skill(GetName()) + " while airborne."));
		}

		if (player.hasPotionEffect(PotionEffectType.SLOW))
		{
			UtilPlayer.message(player, F.main(GetClassType().name(), "You cannot use " + F.skill(GetName()) + " while Slowed."));
			return false;
		}

		//Wall Kick
		if (WallJump(player, level))
			return false;

		return true;
	}

	public void DoLeap(Player player, int level, boolean wallkick)
	{
		//Action
		if (!wallkick)
			UtilAction.velocity(player, 1.2, 0.2, 1, true);
		else
		{
			Vector vec = player.getLocation().getDirection();
			vec.setY(0);
			UtilAction.velocity(player, vec, 0.7, false, 0, 0.7, 2, true);
		}

		//Inform
		if (!wallkick)
			UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
		else
			UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill("Wall Kick") + "."));

		//Effect
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 80);
		player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 2f, 1.2f);
	}

	@Override
	public void Skill(Player player, int level) 
	{
		DoLeap(player, level, false);
	}

	public boolean WallJump(Player player, int level)
	{
		if (level == 0)		return false;

		//Recharge & Energy
		if (!Recharge.Instance.use(player, "Wall Kick", 1500, false, false))
			return false;

		//Direction
		Vector vec = player.getLocation().getDirection();

		//Logic
		boolean xPos = true;
		boolean zPos = true;

		if (vec.getX() < 0)		xPos = false;
		if (vec.getZ() < 0)		zPos = false;

		for (int y=0 ; y<=0 ; y++)
		{
			for (int x=-1 ; x<=1 ; x++)
			{
				for (int z=-1 ; z<=1 ; z++)
				{
					if (x == 0 && z == 0)
						continue;

					if (	(xPos && x > 0) ||
							(zPos && z > 0) ||
							(!xPos && x < 0) ||
							(!zPos && z < 0))
						continue;						

					if (UtilBlock.airFoliage(player.getLocation().getBlock().getRelative(x, y, z)))
						continue;

					Block forward = null;

					//Forward Down
					if (Math.abs(vec.getX()) > Math.abs(vec.getZ()))
					{
						if (xPos)	forward = player.getLocation().getBlock().getRelative(1, 0, 0);
						else		forward = player.getLocation().getBlock().getRelative(-1, 0, 0);	
					}
					else
					{ 
						if (zPos)	forward = player.getLocation().getBlock().getRelative(0, 0, 1);
						else		forward = player.getLocation().getBlock().getRelative(0, 0, -1);	
					}

					if (!UtilBlock.airFoliage(forward))
						continue;

					//Forward Up
					if (Math.abs(vec.getX()) > Math.abs(vec.getZ()))
					{
						if (xPos)	forward = player.getLocation().getBlock().getRelative(1, 1, 0);
						else		forward = player.getLocation().getBlock().getRelative(-1, 1, 0);	 
					}
					else
					{
						if (zPos)	forward = player.getLocation().getBlock().getRelative(0, 1, 1);
						else		forward = player.getLocation().getBlock().getRelative(0, 1, -1);	
					}

					if (!UtilBlock.airFoliage(forward))
						continue;

					DoLeap(player, level, true);

					return true;
				}
			}
		}

		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.FALL)
			return;

		Player player = event.GetDamageePlayer();
		if (player == null)		return;

		int level = getLevel(player);
		if (level == 0)			return;

		event.AddMod(null, GetName(), -2, false);
	}

	@Override
	public void Reset(Player player) 
	{

	}
}