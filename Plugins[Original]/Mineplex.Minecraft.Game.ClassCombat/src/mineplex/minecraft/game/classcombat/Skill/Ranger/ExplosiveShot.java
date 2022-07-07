package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.bukkit.Location;
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
import org.bukkit.event.entity.ProjectileHitEvent;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ExplosiveShot extends SkillActive
{

	private final Set<Arrow> _arrows = new HashSet<>();
	private final Set<Player> _active = new HashSet<>();

	public ExplosiveShot(SkillFactory skills, String name, ClassType classType, SkillType skillType,
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
				"Prepare an explosive shot;",
				"Your next arrow will explode on",
				"impact, dealing up to 10 damage",
				"and knockback. ",
				" ",
				"Explosion radius of #4.5#0.5",
				});
		
		setAchievementSkill(true);
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
	public void BowShoot(EntityShootBowEvent event)
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

		_arrows.add((Arrow)event.getProjectile());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void hitEntityTrigger(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile projectile = event.GetProjectile();
		if (projectile == null)	
			return;

		if (!_arrows.remove(event.GetProjectile()))
			return;

		event.SetCancelled(GetName());

		Location loc = event.GetDamageeEntity().getLocation().subtract(event.GetProjectile().getVelocity().normalize().multiply(0.1));
		
		trigger((Arrow)event.GetProjectile(), loc);
	}

	@EventHandler
	public void hitBlockTrigger(ProjectileHitEvent event)
	{
		Projectile proj = event.getEntity();

		if (!_arrows.contains(proj))
			return;

		if (proj.getShooter() == null)
			return;

		if (!(proj.getShooter() instanceof Player))
			return;

		Player damager = (Player)proj.getShooter();
		int level = getLevel(damager);
		if (level == 0)		return;

		final Arrow arrow = (Arrow)proj;

		Factory.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Factory.getPlugin(), new Runnable()
		{
			public void run()
			{
				try
				{
					//If it hasnt already triggered (via damage)
					if (_arrows.remove(arrow))
						trigger(arrow, arrow.getLocation());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, 0);

		//Remove
		proj.remove();
	}

	@EventHandler
	public void cleanTrigger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Iterator<Arrow> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Arrow arrow = arrowIterator.next();

			if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround() || arrow.getTicksLived() > 60)
			{
				arrowIterator.remove();
				trigger(arrow, arrow.getLocation());
			}
		}
	}

	public void trigger(Arrow arrow, Location loc)
	{
		if (arrow == null)
			return;

		if (arrow.getShooter() == null || !(arrow.getShooter() instanceof Player))
		{
			arrow.remove();
			return;
		}

		Player player = (Player)arrow.getShooter();

		//Level
		int level = getLevel(player);
		if (level == 0)			
			return;

		//Velocity Players
		HashMap<LivingEntity,Double> hitMap = UtilEnt.getInRadius(loc, 4.5 + (level * 0.5));
		for (LivingEntity cur : hitMap.keySet())
		{	
			double range = hitMap.get(cur);

			//Condition
			Factory.Condition().Factory().Falling(GetName(), cur, player, 6, false, true);

			//Damage Event
			Factory.Damage().NewDamageEvent(cur, player, null, 
					DamageCause.CUSTOM, 10 * range, false, true, false,
					player.getName(), GetName());	

			//Velocity
			UtilAction.velocity(cur, UtilAlg.getTrajectory2d(loc, cur.getLocation().add(0, 1, 0)), 
					0.4 + 1 * range, false, 0, 0.2 + 0.6 * range, 1.2, true);

			//Inform
			if (cur instanceof Player)
				UtilPlayer.message((Player)cur, F.main(GetClassType().name(), F.name(player.getName()) +" hit you with " + F.skill(GetName(level)) + "."));
		}
		
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, loc, 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		loc.getWorld().playSound(loc, Sound.EXPLODE, 2f, 0.75f);
	}

	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Entity ent : _arrows)
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, ent.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}
}
