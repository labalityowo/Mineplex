package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class RopedArrow extends SkillActive
{
	private HashSet<Entity> _arrows = new HashSet<Entity>();
	private HashSet<Player> _roped = new HashSet<Player>();

	public RopedArrow(SkillFactory skills, String name, ClassType classType, SkillType skillType,
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
				"Prepare a roped arrow;",
				"Your next arrow will pull you",
				"in after it hits.",
				});
	}

	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Action
		_roped.add(player);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You prepared " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);
	}

	@EventHandler
	public void handleShootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		Player player = (Player)event.getEntity();

		if (!_roped.remove(player))
			return;

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You fired " + F.skill(GetName(getLevel(player))) + "."));

		_arrows.add(event.getProjectile());
		
		UtilEnt.leash(player, event.getProjectile(), false, false);
	}

	@EventHandler
	public void ArrowHit(ProjectileHitEvent event)
	{
		if (!_arrows.remove((Entity)event.getEntity()))
			return;

		Projectile proj = event.getEntity();

		if (proj.getShooter() == null)
			return;

		if (!(proj.getShooter() instanceof Player))
			return;

		//Level
		int level = getLevel(((Player)proj.getShooter()));
		if (level == 0)			return;

		Vector vec = UtilAlg.getTrajectory((Entity)proj.getShooter(), proj);
		double mult = (proj.getVelocity().length() / 3d);
		
		//Action
		UtilAction.velocity((Entity)proj.getShooter(), vec, 
				0.4 + mult, false, 0, 0.3 * mult, 1.2 * mult, true);

		//Effect
		proj.getWorld().playSound(proj.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Iterator<Entity> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Entity arrow = arrowIterator.next();
			
			if (arrow.isDead() || !arrow.isValid())
				arrowIterator.remove();
		}
	}
	
	@Override
	public void Reset(Player player) 
	{
		_roped.remove(player);
	}
}
