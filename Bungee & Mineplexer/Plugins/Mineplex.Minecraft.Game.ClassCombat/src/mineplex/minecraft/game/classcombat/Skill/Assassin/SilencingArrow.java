package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class SilencingArrow extends SkillActive
{

	private final Set<Entity> _arrows = new HashSet<>();
	private final Set<Player> _silence = new HashSet<>();

	public SilencingArrow(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Your next arrow will Silence",
				"target for 3 seconds.",
				"",
				"Silence stops skills being used."
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
		_silence.add(player);

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

		if (!_silence.remove(player))
			return;

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You fired " + F.skill(GetName(getLevel(player))) + "."));

		_arrows.add(event.getProjectile());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile projectile = event.GetProjectile();
		if (projectile == null)	return;

		if (!_arrows.contains((Entity)projectile))
			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		//Level
		int level = getLevel(damager);
		if (level == 0)			return;

		//Confuse
		Factory.Condition().Factory().Silence(GetName(), damagee, damager, 3, true, true);

		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);

		//Inform
		UtilPlayer.message(event.GetDamageePlayer(), F.main(GetClassType().name(), F.name(damager.getName()) +" hit you with " + F.skill(GetName(level)) + "."));
		UtilPlayer.message(damager, F.main(GetClassType().name(), "You hit " + F.name(UtilEnt.getName(damagee)) +" with " + F.skill(GetName(level)) + "."));
		
		//Damage
		event.AddMod(damager.getName(), GetName(), 0, true);
		
		_arrows.remove(projectile);
		
		//Remove
		projectile.remove();
	}

	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Entity ent : _arrows)
		{
			UtilParticle.PlayParticle(ParticleType.SPELL, ent.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
		}
	}
	
	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Iterator<Entity> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Entity arrow = arrowIterator.next();
			
			if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround())
				arrowIterator.remove();
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_silence.remove(player);
	}
}
