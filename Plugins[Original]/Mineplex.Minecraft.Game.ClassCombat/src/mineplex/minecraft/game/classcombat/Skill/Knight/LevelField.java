package mineplex.minecraft.game.classcombat.Skill.Knight;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCrafting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class LevelField extends Skill
{

	private final Set<Player> _active = new HashSet<>();

	public LevelField(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels)
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[]
				{
						"Drop Axe/Sword to Use.",
						"",
						"You deal X more damage.",
						"You take X less damage.",
						"X = (Nearby Enemies) - (Nearby Allies)",
						"Players within #4#2 Blocks are considered.",
						"",
						"Damage can be altered a maximum of #1#1.",
						"You can not deal less damage, or take",
						"more damage via this.",
						"",
						"Lasts for #6#1."
				});
	}

	@Override
	public String GetRechargeString()
	{
		return "Recharge: #16#-1 Seconds";
	}

	@EventHandler
	public void Use(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (!(player.getOpenInventory().getTopInventory() instanceof CraftInventoryCrafting))
		{
			return;
		}

		int level = getLevel(player);

		if (level == 0 || !UtilGear.isWeapon(event.getItemDrop().getItemStack()) || !Recharge.Instance.usable(player, GetName(), true))
		{
			return;
		}

		event.setCancelled(true);

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		UtilServer.CallEvent(trigger);

		if (trigger.IsCancelled())
		{
			return;
		}

		long start = System.currentTimeMillis();
		_active.add(player);

		player.sendMessage(F.main("Skill", "You used " + F.skill(GetName(level)) + "."));

		Factory.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!player.isOnline() || UtilTime.elapsed(start, 6000 + (1000 * level)))
				{
					Recharge.Instance.use(player, GetName(), 16000 - (1000 * level), true, false);
					Reset(player);
					cancel();
					return;
				}

				UtilParticle.playParticleFor(player, ParticleType.HAPPY_VILLAGER, player.getLocation().add(0, 0.5, 0), 1, 0.3F, 1, 0, 1, ViewDist.LONG);
			}
		}, 0, 2);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void alterDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled() || event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damagee = event.GetDamageePlayer(), damager = event.GetDamagerPlayer(false);

		if (damagee == null || damager == null)
		{
			return;
		}

		{
			int level = getLevel(damagee);

			if (level == 0)
			{
				return;
			}

			event.AddMod(damagee.getName(), GetName(), getAlteration(damagee, level, false), false);
		}
		{
			int level = getLevel(damager);

			if (level == 0)
			{
				return;
			}

			event.AddMod(damager.getName(), GetName(), getAlteration(damager, level, true), false);
		}
	}

	private int getAlteration(Player player, int level, boolean positive)
	{
		int alt = 0;

		for (Player other : UtilPlayer.getNearby(player.getLocation(), 4 + (2 * level)))
		{
			if (player.equals(other))
			{
				alt--;
			}
			else if (Factory.Relation().canHurt(player, other))
			{
				alt++;
			}
			else
			{
				alt--;
			}
		}

		int limit = 1 + level;

		alt = Math.min(alt, limit);
		alt = Math.max(alt, -limit);

		if (positive)
		{
			return Math.max(0, alt);
		}
		else
		{
			return Math.min(0, -alt);
		}
	}

	@Override
	public void Reset(Player player)
	{
		_active.remove(player);
	}
}
