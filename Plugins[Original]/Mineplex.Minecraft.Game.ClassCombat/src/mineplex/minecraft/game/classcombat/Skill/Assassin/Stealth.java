package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.HashSet;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class Stealth extends Skill
{
	private HashSet<Player> _active = new HashSet<Player>();

	public Stealth(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Drop Axe/Sword to Toggle",
				"",
				"Move stealthily, becoming completely",
				"Invisible, but also Slow #4#-1.",
				"",
				"Stealth ends if you an enemy comes",
				"within #10#-2 Blocks of you, or you attack.",
				});
	}

	@Override
	public String GetEnergyString()
	{
		return "Energy: #13#1 per Second";
	}

	@EventHandler
	public void Crouch(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (getLevel(player) == 0)
			return;

		if (!UtilGear.isWeapon(event.getItemDrop().getItemStack()))
			return;

		event.setCancelled(true);

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		UtilServer.getServer().getPluginManager().callEvent(trigger);
		if (trigger.IsCancelled())
			return;

		if (!_active.remove(player))
		{
			if (player.hasPotionEffect(PotionEffectType.SLOW))
			{
				UtilPlayer.message(player, F.main(GetClassType().name(), "You cannot use " + F.skill(GetName()) + " while Slowed."));
				return;
			}

			if (!UtilTime.elapsed(Factory.Combat().getLog(player).GetLastCombat(), 4000))
			{
				UtilPlayer.message(player, F.main(GetClassType().name(), "You cannot use " + F.skill(GetName()) + " while in Combat."));
				return;
			}

			Add(player);
		}
		else
		{
			Remove(player, player);
		}
	}

	public void Add(Player player)
	{
		_active.add(player);

		int level = getLevel(player);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "Stealth: " + F.oo("Enabled", true)));

		//Conditions
		Factory.Condition().Factory().Cloak(GetName(), player, player, 120000, false, true);
		Factory.Condition().Factory().Slow(GetName(), player, player, 120000, 3-level, false, false, false, true);

		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 0.5f, 0.5f);

		//Effect
		UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, player.getLocation(), 
				(float)(Math.random() - 0.5), (float)(Math.random() * 1.4), (float)(Math.random() - 0.5), 0, 10,
				ViewDist.MAX, UtilServer.getPlayers());
	}

	public void Remove(Player player, LivingEntity source)
	{
		_active.remove(player);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "Stealth: " + F.oo("Disabled", false)));

		//Conditions
		Factory.Condition().EndCondition(player, null, GetName());

		//Effect
		UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, player.getLocation(), 
				(float)(Math.random() - 0.5), (float)(Math.random() * 1.4), (float)(Math.random() - 0.5), 0, 10,
				ViewDist.MAX, UtilServer.getPlayers());
	}

	@EventHandler
	public void EndProx(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : GetUsers())
		{
			int level = getLevel(cur);
			if (level == 0)		continue;

			//Proximity Decloak
			if (_active.contains(cur)) 	
				for (Player other : cur.getWorld().getPlayers())
				{
					if (other.equals(cur))
						continue;

					if (UtilMath.offset(cur, other) > 8 - 2*level)
						continue;

					if (!Factory.Relation().canHurt(cur, other))
						continue;

					Remove(cur, other);
					break;
				}
		}
	}

	@EventHandler 
	public void EndInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!_active.contains(player))
			return;

		Remove(player, player);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void EndDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee != null)	return;
		{
			if (_active.contains(damagee))
			{
				Remove(damagee, event.GetDamagerEntity(true));
			}
		}

		Player damager = event.GetDamagerPlayer(true);
		if (damager != null)	return;
		{
			if (_active.contains(damager))
			{
				Remove(damager, event.GetDamagerEntity(true));
			}
		}
	}

	@EventHandler
	public void Energy(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.contains(cur))
				continue;

			//Level
			if (getLevel(cur) == 0)
			{
				Remove(cur, null);	
				continue;
			}

			//Silence
			if (Factory.Condition().IsSilenced(cur, null))
			{
				Remove(cur, null);
				continue;
			}

			if (!Factory.Energy().Use(cur, GetName(), 0.9 - (0.1 * getLevel(cur)), true, false))
			{
				Remove(cur, null);
				continue;
			}
		}	
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
		Factory.Condition().EndCondition(player, ConditionType.CLOAK, GetName());
	}
}
