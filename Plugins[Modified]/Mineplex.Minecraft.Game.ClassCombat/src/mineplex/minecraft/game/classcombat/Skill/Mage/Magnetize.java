package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Magnetize extends SkillActive
{
	private HashSet<Player> _active = new HashSet<Player>();

	public Magnetize(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"SKILL NEEDS REPLACING",
				});
	}

	@Override
	public String GetEnergyString()
	{
		return "Energy: 16 per Second";
	}

	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		UtilPlayer.message(player, F.main("Skill", "This skill is currently being re-worked."));
		return false;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		_active.add(player);
	}


	@EventHandler
	public void Energy(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.contains(cur))
				continue;

			if (!UtilPlayer.isBlocking(cur))
			{
				_active.remove(cur);
				continue;
			}

			//Level
			int level = getLevel(cur);
			if (level == 0)		
			{
				_active.remove(cur);
				continue;
			}

			if (!Factory.Energy().Use(cur, GetName(), 2, true, true))
			{
				_active.remove(cur);
				continue;
			}

			//Effect
			cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, 42);

			//Pull		
			for (int i=0 ; i<=5+level ; i++)
				Pull(cur, cur.getEyeLocation().add(cur.getLocation().getDirection().multiply(i)));
		}
	}
	
	public void Pull(Player player, Location loc)
	{
		for (Player other : UtilPlayer.getNearby(loc, 2))
		{
			if (player.equals(other))
				continue;

			if (!Factory.Relation().canHurt(player, other))		
				continue;

			if (UtilMath.offset(player, other) < 2)		
				continue;

			UtilAction.velocity(other, UtilAlg.getTrajectory2d(other, player), 
					0.2, false, 0, 0, 1, false);
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}
}
