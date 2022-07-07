package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class HealingShot extends SkillActive
{

	private final Set<Entity> _arrows = new HashSet<>();
	private final Set<Player> _active = new HashSet<>();

	public HealingShot(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Prepare a Healing Shot;",
				"",
				"Your next arrow will give its target",
				"Regeneration 3 for #3#1 seconds,",
				"and remove all negative effects.",
				"",
				"Self hits give Regeneration 2.",
				"",
				"Gives Nausea to enemies for #5#1 seconds.",
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
		_active.add(player);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You prepared " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);
	}

	@EventHandler
	public void ShootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		Player player = (Player)event.getEntity();

		if (!_active.remove(player))
			return;

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You fired " + F.skill(GetName(getLevel(player))) + "."));

		_arrows.add(event.getProjectile());
	}

	@EventHandler
	public void arrowHit(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);
		Projectile projectile = event.GetProjectile();

		if (!_arrows.remove(projectile))
		{
			return;
		}

		//Level
		int level = getLevel(damager);
		if (level == 0)
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();

		if (damagee == null)
		{
			return;
		}

		projectile.remove();

		//Regen
		if (!Factory.Relation().canHurt(damager, damagee) || damager.equals(damagee))
		{
			Factory.Condition().Factory().Regen(GetName(), damagee, damager, 3 + level, damager.equals(damagee) ? 1 : 2, false, false, false);
			
			//Remove Bad
			damagee.setFireTicks(0);
			damagee.removePotionEffect(PotionEffectType.SLOW);
			damagee.removePotionEffect(PotionEffectType.POISON);
			damagee.removePotionEffect(PotionEffectType.CONFUSION);
			damagee.removePotionEffect(PotionEffectType.WEAKNESS);
		}	
		else
		{
			Factory.Condition().Factory().Confuse(GetName(), damagee, damager, 5 + level, 2, false, false, false);
		}

		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.LEVEL_UP, 1f, 1.5f);
		damagee.getWorld().playEffect(damagee.getLocation(), Effect.STEP_SOUND, 115);
		damagee.playEffect(EntityEffect.HURT);

		//Inform
		UtilPlayer.message(damagee, F.main(GetClassType().name(), 
				F.name(damager.getName()) +" hit you with " + F.skill(GetName(level)) + "."));

		UtilPlayer.message(damager, F.main(GetClassType().name(), 
				"You hit " + F.name(UtilEnt.getName(damagee)) +" with " + F.skill(GetName(level)) + "."));
		
		//Particles
		UtilParticle.PlayParticleToAll(ParticleType.HEART, damagee.getLocation(),
				(float)(Math.random() - 0.5), (float)(Math.random() + 0.5), (float)(Math.random() - 0.5), 2f, 12,
				ViewDist.LONG);
		
		//Remove
		projectile.remove();
	}
	
	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Entity ent : _arrows)
		{
			UtilParticle.PlayParticleToAll(ParticleType.HEART, ent.getLocation(), 0, 0, 0, 0, 1, ViewDist.LONG);
		}
	}

	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_arrows.removeIf(arrow -> !arrow.isValid() || arrow.isOnGround());
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}
}
