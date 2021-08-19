package mineplex.minecraft.game.classcombat.Skill.Mage;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.event.FireballHitEntityEvent;

public class FireBlast extends SkillActive
{
	public FireBlast(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Launch a fireball which explodes on impact",
				"dealing large knockback to enemies within",
				"#6#0.5 Blocks range. Also ignites enemies",
				"for up to #2#2 seconds."
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

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Action
		LargeFireball ball = player.launchProjectile(LargeFireball.class);
		ball.setShooter(player);
		ball.setIsIncendiary(false);		
		ball.setYield(0);
		ball.setBounce(false);
		ball.teleport(player.getEyeLocation().add(player.getLocation().getDirection().multiply(1)));
		ball.setVelocity(new Vector(0,0,0));

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1f, 0.8f);
	}

	@EventHandler
	public void Collide(ProjectileHitEvent event)
	{
		Projectile proj = event.getEntity();

		if (!(proj instanceof LargeFireball))
			return;

		if (proj.getShooter() == null)
			return;

		if (!(proj.getShooter() instanceof Player))
			return;
		
		Player player = (Player)proj.getShooter();

		//Level
		int level = getLevel(player);
		if (level == 0)				return;

		//Velocity Players
		UtilEnt.getInRadius(proj.getLocation(), 5.5 + (0.5 * level)).forEach((cur, range) ->
		{
			FireballHitEntityEvent fireballEvent = new FireballHitEntityEvent(proj, cur);
			UtilServer.CallEvent(fireballEvent);
			
			if (fireballEvent.isCancelled())
			{
				return;
			}
			
			//Damage Event
			Factory.Condition().Factory().Ignite(GetName(), cur, player, (2 + (1 * level)) * range, false, false);

			//Condition
			Factory.Condition().Factory().Falling(GetName(), cur, player, 10, false, true);
			
			//Velocity
			UtilAction.velocity(cur, UtilAlg.getTrajectory(proj.getLocation().add(0, -0.5, 0), cur.getEyeLocation()), 1.6 * range, false, 0, 0.8 * range, 1.2, true);
		});
	}

	@Override
	public void Reset(Player player) 
	{

	}
}
