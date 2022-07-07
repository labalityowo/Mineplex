package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCrafting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class Intimidation extends Skill
{

	private final Set<Player> _active = new HashSet<>();
	private final DustSpellColor _color = new DustSpellColor(Color.PURPLE);

	public Intimidation(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Drop Axe/Sword to Use.",
				"",
				"You intimidate nearby enemies;",
				"Enemies within #10#1 blocks receive Slow 1.",
				"",
				"Lasts for #10#1."
				});
	}

	@Override
	public String GetRechargeString()
	{
		return "Recharge: #15#-1.5 Seconds.";
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

		if (level == 0 || !UtilGear.isWeapon(event.getItemDrop().getItemStack()) || _active.contains(player) || !Recharge.Instance.usable(player, GetName(), true))
		{
			return;
		}

		event.setCancelled(true);

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		Bukkit.getServer().getPluginManager().callEvent(trigger);

		if (trigger.IsCancelled())
		{
			return;
		}

		_active.add(player);
		player.getWorld().playSound(player.getLocation(), Sound.HORSE_SKELETON_HIT, 1, 1);
		player.sendMessage(F.main("Skill", "You used " + F.skill(GetName(level)) + "."));

		long start = System.currentTimeMillis(), length = 10000 + (level * 1000);

		Factory.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!_active.contains(player) || UtilTime.elapsed(start, length))
				{
					Recharge.Instance.use(player, GetName(), 15000 - (1500 * level), true, false);
					Reset(player);
					cancel();
					return;
				}

				player.setExp(1 - ((System.currentTimeMillis() - start) / (float) length));

				Location location = player.getLocation();

				new ColoredParticle(ParticleType.RED_DUST, _color, location.clone().add(Math.random() - 0.5, Math.random() + 0.5, Math.random() - 0.5))
						.display();

				UtilPlayer.getInRadius(location, 10 + level).forEach((other, scale) ->
				{
					if (other.equals(player) || !Factory.Relation().canHurt(player, other))
					{
						return;
					}

					Factory.Condition().Factory().Slow(GetName(), other, player, 0.9, 0, false, true, false, true);
				});
			}
		}, 0, 1);
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
		player.setExp(0);
	}
}
