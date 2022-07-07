package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class WhirlwindAxe extends SkillActive
{
	public WhirlwindAxe(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Whirl your axes around rapidly dealing",
				"up to #3#1 damage to enemies within",
				"#4#1 blocks, pulling them towards you.",
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
	public void Skill(final Player player, int level) 
	{
		//Pull + Damage
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), 4d + 1 * level);
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(player))
				continue;
			
			if (cur instanceof Player)
				if (!Factory.Relation().canHurt((Player)cur, player))
					continue;
			
			//Damage Event
			if (UtilMath.offset(cur, player) < 4)
			{
				Factory.Damage().NewDamageEvent(cur, player, null, 
						DamageCause.CUSTOM, 2 + (1 + level) * targets.get(cur), false, true, false,
						player.getName(), GetName());
			}
				
			//Velocity
			UtilAction.velocity(cur, 
					UtilAlg.getTrajectory2d(cur.getLocation().toVector(), player.getLocation().toVector()), 
					1.6 - 0.6*targets.get(cur), true, 0, 0.2, 1, true);
			
			//Inform
			if (cur instanceof Player)
				UtilPlayer.message((Player)cur, F.main(GetClassType().name(), F.name(player.getName()) +" hit you with " + F.skill(GetName(level)) + "."));	
		}
				
		//Animation
		for (double i=0 ; i<Math.PI * 2 ; i += 0.1)
		{
			final double j = i;
			
			final int ticksLived = player.getTicksLived();
			final Location loc = player.getLocation();
			
			Bukkit.getServer().getScheduler().runTaskLater(Factory.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					double x = Math.sin(j + (ticksLived/50d)) * (j%(Math.PI/2d)) * 3;
					double z = Math.cos(j + (ticksLived/50d)) * (j%(Math.PI/2d)) * 3;
					
					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc.clone().add(x, 1, z), 0f, 0f, 0f, 0, 1,
							ViewDist.LONG, UtilServer.getPlayers());
					
					x = Math.sin(j + (ticksLived/50d) + Math.PI/4) * (j%(Math.PI/2d)) * 3;
					z = Math.cos(j + (ticksLived/50d) + Math.PI/4) * (j%(Math.PI/2d)) * 3;
					
					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc.clone().add(x, 1, z), 0f, 0f, 0f, 0, 1,
							ViewDist.LONG, UtilServer.getPlayers());
					
					//Sound
					player.getWorld().playSound(player.getLocation(), Sound.STEP_WOOL, 2f, 1f + (float)((j%(Math.PI/2d))/(Math.PI/2)));
				}
			}, (long) ((Math.PI/2d - (j%(Math.PI/2d))) * 8));
		}
		
		
		
		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
	}

	@Override
	public void Reset(Player player)
	{
		
	}
}
