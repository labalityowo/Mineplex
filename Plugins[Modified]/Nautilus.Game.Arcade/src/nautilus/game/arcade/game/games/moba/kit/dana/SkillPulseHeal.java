package nautilus.game.arcade.game.games.moba.kit.dana;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillPulseHeal extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Heals nearby allies and minions."
	};

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.INK_SACK, (byte) 10).build();

	public SkillPulseHeal(int slot)
	{
		super("Pulse Heal", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);
		setCooldown(8000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		useSkill(player);

		for (LivingEntity entity : UtilEnt.getInRadius(player.getLocation(), 7).keySet())
		{
			// Don't heal enemies
			if (!isTeamDamage(entity, player))
			{
				continue;
			}

			MobaUtil.heal(entity, player, entity.equals(player) ? 2 : 4);
		}

		displayPulse(player.getLocation().add(0, 0.5, 0));
	}

	private void displayPulse(Location location)
	{
		Manager.runSyncTimer(new BukkitRunnable()
		{

			double theta = 0;
			double radius = 0;

			@Override
			public void run()
			{
				if (radius > 7)
				{
					cancel();
					return;
				}

				for (double theta2 = 0; theta2 < 2 * Math.PI; theta2 += Math.PI / 3)
				{
					double x = radius * Math.sin(theta + theta2);
					double z = radius * Math.cos(theta + theta2);

					location.add(x, 0.5, z);

					UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, location, 0, 0, 0, 0.1F, 1, ViewDist.LONG);

					location.subtract(x, 0.5, z);
				}

				theta += Math.PI / 100;
				radius += 0.2;
			}
		}, 0, 1);
	}
}
