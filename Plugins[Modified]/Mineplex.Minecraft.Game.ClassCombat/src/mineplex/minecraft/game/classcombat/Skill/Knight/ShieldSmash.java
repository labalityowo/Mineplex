package mineplex.minecraft.game.classcombat.Skill.Knight;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class ShieldSmash extends SkillActive
{
	public ShieldSmash(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Smash your shield into an enemy,",
				"dealing #1.6#0.2 knockback."
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

		if (!mineplex.core.recharge.Recharge.Instance.use(player, GetName() + " Cooldown", 250, false, false))
			return false;

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//AoE Area
		Location loc = player.getLocation();
		loc.add(player.getLocation().getDirection().setY(0).normalize().multiply(1.5));
		loc.add(0, 0.8, 0);
		
		//Particle
		UtilParticle.PlayParticle(ParticleType.CLOUD, loc, 0, 0, 0, 0.05f, 6,
				ViewDist.LONG, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, loc, 0, 0, 0, 0, 1,
				ViewDist.LONG, UtilServer.getPlayers());
		
		boolean hit = false;

		for (Entity other : player.getWorld().getEntities())
		{
			if (!(other instanceof LivingEntity))
				continue;
			
			LivingEntity cur = (LivingEntity)other;

			if (cur.equals(player))
				continue;
			
			if (UtilPlayer.isSpectator(cur))
				continue;

			if (UtilMath.offset(loc, cur.getLocation()) > 2.5)
				continue;

			hit = true;
			
			Vector dir = player.getLocation().getDirection();
			if (dir.getY() < 0)
				dir.setY(0);
			
			//Velocity
			UtilAction.velocity(cur, dir, 1.6 + 0.2 * level, false, 0, 0.3, 0.8 + 0.05 * level, true);
			
			//Condition
			Factory.Condition().Factory().Falling(GetName(), cur, player, 10, false, true);
			
			//Inform
			UtilPlayer.message(cur, F.main(GetClassType().name(), F.name(player.getName()) +" hit you with " + F.skill(GetName(level)) + "."));
		}
		
		if (hit)
		{
			//Inform
			UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName(level)) + "."));
			
			//Sound
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 1f, 0.9f);
		}
		else
		{
			//Inform
			UtilPlayer.message(player, F.main("Skill", "You missed " + F.skill(GetName(level)) + "."));
		}	
	}

	@Override
	public void Reset(Player player) 
	{

	}
}
