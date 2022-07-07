package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillChargeBow;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Overcharge extends SkillChargeBow
{

	private final Map<Arrow, Double> _arrows = new WeakHashMap<>();

	public Overcharge(SkillFactory skills, String name, ClassType classType,
			SkillType skillType, int cost, int maxLevel)
	{
		super(skills, name, classType, skillType, cost, maxLevel, 
				0.0225f, 0.005f, false, true);
	
		SetDesc(new String[] 
				{
				"Charge your bow to deal bonus damage.",
				"",
				GetChargeString(),
				"",
				"Deals up to #1.5#1.5 bonus damage.",
				"Fully charging increases arrow speed by #5#5 %"
				});
	}
	
	@Override
	public void DoSkillCustom(Player player, float charge, Arrow arrow)
	{
		double damage =  charge * (1.5 * getLevel(player));
		_arrows.put(arrow, damage);
		
		if (charge >= 1)
		{
			//Increase Speed
			arrow.setVelocity(arrow.getVelocity().multiply(1 + (.05 * getLevel(player))));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void ArrowHit(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile projectile = event.GetProjectile();
		if (projectile == null)	return;

		if (!_arrows.containsKey(projectile))
			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		//Level
		int level = getLevel(damager);
		if (level == 0)			return;

		double damage = _arrows.remove(projectile);
		
		//Damage
		event.AddMod(damager.getName(), GetName(), damage, true);

		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.HURT_FLESH, 1f, 0.5f);
	}
	
	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Entity ent : _arrows.keySet())
		{
			UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, ent.getLocation(), null, 0, 1, ViewDist.LONG);
		}
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_arrows.keySet().removeIf(arrow -> !arrow.isValid() || arrow.isOnGround());
	}
}