package mineplex.minecraft.game.classcombat.Skill.Knight;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class BullsCharge extends SkillActive
{
	public BullsCharge(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Charge forwards with Speed II for",
				"#3#1 seconds. If you attack during this",
				"time, your target receives Slow 2",
				"for #2.5#0.5 seconds, as well as no knockback."
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
		Factory.Condition().Factory().Speed(GetName(), player, player, 3 + level, 1, false, true, true);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 1.5f, 0f);
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 49);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(true);
		LivingEntity damagee = event.GetDamageeEntity();

		if (damager == null || damagee == null)
			return;
		
		//Isn't using Bulls Charge
		if (!Factory.Condition().HasCondition(damager, ConditionType.SPEED, GetName()))
			return;

		//Level
		int level = getLevel(damager);
		if (level == 0)				return;

		//Condition
		Factory.Condition().Factory().Slow(GetName(), damagee, damager, 2.5 + (0.5 * level), 1, false, true, true, true);
		Factory.Condition().EndCondition(damager, ConditionType.SPEED, GetName());

		//Damage
		//event.AddMod(damager.getName(), getName(), level, true);
		event.SetKnockback(false);
		
		//Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.ENDERMAN_SCREAM, 1.5f, 0f);
		damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_METAL, 1.5f, 0.5f);

		//Inform
		UtilPlayer.message(damagee, F.main(GetClassType().name(), 
				F.name(damager.getName()) + " hit you with " + F.skill(GetName(level)) + "."));
		
		UtilPlayer.message(damager, F.main(GetClassType().name(), 
				"You hit " + F.name(UtilEnt.getName(damagee)) +" with " + F.skill(GetName(level)) + "."));
	}
	
	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : GetUsers())
		{
			if (player.hasPotionEffect(PotionEffectType.SPEED))
				UtilParticle.PlayParticle(ParticleType.CRIT, player.getLocation(), 
					(float)(Math.random() - 0.5), 0.2f + (float)(Math.random() * 1), (float)(Math.random() - 0.5), 0, 3,
					ViewDist.LONG, UtilServer.getPlayers());
		}
	}

	@Override
	public void Reset(Player player) 
	{
		
	}
}
