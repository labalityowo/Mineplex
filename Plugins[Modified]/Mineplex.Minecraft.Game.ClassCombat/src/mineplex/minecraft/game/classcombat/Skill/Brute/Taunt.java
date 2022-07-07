package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Taunt extends SkillActive
{
	private HashMap<LivingEntity, Long> _live = new HashMap<LivingEntity, Long>();

	public Taunt(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Shout a taunt that forces all",
				"enemies within #6#0 blocks to",
				"run towards you for #1.5#0.5 seconds."
				});
		
		this.setAchievementSkill(true);
	}

	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
	}

	@EventHandler
	public void End(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		//End
		for (Player player : GetUsers())
		{
			if (!UtilEnt.isGrounded(player))
				continue;

			if (!_live.containsKey(player))
				continue;

			int level = getLevel(player);
			if (level == 0)		continue;

			if (!UtilTime.elapsed(_live.get(player), 1500 + 500*level))  
				continue;

			_live.remove(player);			
		}	

		//Collide
		for (Player player : GetUsers())
			if (_live.containsKey(player))
				for (Player other : player.getWorld().getPlayers())
					if (!UtilPlayer.isSpectator(other))
						if (!other.equals(player))
							if (Factory.Relation().canHurt(player, other))
								if (UtilMath.offset(player, other) < 7 && UtilMath.offset(player, other) > 2)
								{
									UtilAction.velocity(other, UtilAlg.getTrajectory(other, player), 0.4, false, 0, 0, 0.4, false);
								}
	}

	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Entity ent : _live.keySet())
		{
			UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, ent.getLocation().add(0, 2, 0), 
					0, 0, 0, 6f, 8,
					ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_live.remove(player);
	}
}
