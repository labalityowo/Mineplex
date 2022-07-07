package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SeismicSlam extends SkillActive
{
	public static class SeismicSlamEvent extends PlayerEvent
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		private final List<LivingEntity> _targets;

		public SeismicSlamEvent(Player who, Collection<LivingEntity> targets)
		{
			super(who);

			_targets = new ArrayList<>(targets);
		}

		public List<LivingEntity> getTargets()
		{
			return _targets;
		}
	}

	private HashMap<LivingEntity, Long> _live = new HashMap<LivingEntity, Long>();

	public SeismicSlam(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Jump up and slam back into the ground.",
				"Players within #5.5#0.5 Blocks take up to",
				"#1#1 damage and are smashed away from you.",
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
		Vector vec = player.getLocation().getDirection();
		if (vec.getY() < 0)
			vec.setY(vec.getY() * -1);
		
		UtilAction.velocity(player, vec, 0.6, false, 0, 0.8, 0.8, true);

		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
	}

	@EventHandler
	public void Slam(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetUsers())
		{
			if (!_live.containsKey(player))
				continue;

			int level = getLevel(player);
			if (level == 0)		continue;

			if (!UtilTime.elapsed(_live.get(player), 1000))  
				continue;
			
			if (!UtilTime.elapsed(_live.get(player), 4000) && !UtilEnt.isGrounded(player))
				continue;

			_live.remove(player);

			//Action
			HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), 5.5d + 0.5 * level);
			for (LivingEntity cur : targets.keySet())
			{
				if (cur.equals(player))
					continue;

				//Damage Event
				Factory.Damage().NewDamageEvent(cur, player, null, 
						DamageCause.CUSTOM, (1+level) * targets.get(cur) + 0.5, false, true, false,
						player.getName(), GetName());	

				//Velocity
				UtilAction.velocity(cur, 
						UtilAlg.getTrajectory2d(player.getLocation().toVector(), cur.getLocation().toVector()), 
						0.6 + 2 * targets.get(cur), true, 0, 0.2 + 1.0 * targets.get(cur), 1.4, true);
				
				//Condition
				Factory.Condition().Factory().Falling(GetName(), cur, player, 10, false, true);

				//Inform
				if (cur instanceof Player)
					UtilPlayer.message((Player)cur, F.main(GetClassType().name(), F.name(player.getName()) +" hit you with " + F.skill(GetName(level)) + "."));	
			}

			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);
			for (Block cur : UtilBlock.getInRadius(player.getLocation(), 4d).keySet())
				if (UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)) && !UtilBlock.airFoliage(cur))
					cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, cur.getTypeId());
			
			//Event
			UtilServer.getServer().getPluginManager().callEvent(new SkillEvent(player, GetName(), ClassType.Brute, targets.keySet()));

			Bukkit.getPluginManager().callEvent(new SeismicSlamEvent(player, targets.keySet()));
		}	
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void FallDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.FALL)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		int level = getLevel(damagee);
		if (level == 0)		return;

		double amount = Math.min(3, event.GetDamage());
		
		//Damage
		event.AddMod(damagee.getName(), GetName(), -amount, false);
	}

	@Override
	public void Reset(Player player) 
	{
		_live.remove(player);
	}
}
