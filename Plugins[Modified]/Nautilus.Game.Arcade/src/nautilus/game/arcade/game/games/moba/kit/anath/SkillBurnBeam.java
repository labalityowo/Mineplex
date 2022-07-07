package nautilus.game.arcade.game.games.moba.kit.anath;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SkillBurnBeam extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Fires a single, vertical beam of Flames which move forward in a straight.",
			"It passes through enemies and structures dealing damage",
			"to anything it passes through."
	};

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FIREBALL);

	public SkillBurnBeam(int slot)
	{
		super("Burn Beam", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(9000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		Vector direction = player.getLocation().getDirection().setY(0);

		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2, 0.5F);

		useSkill(player);

		LineParticle particle = new LineParticle(player.getLocation().add(direction), direction, 0.2, 9, ParticleType.LAVA, UtilServer.getPlayers());

		particle.setIgnoreAllBlocks(true);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < 3; i++)
				{
					if (particle.update())
					{
						cancel();
						return;
					}
					else
					{
						UtilParticle.PlayParticleToAll(ParticleType.FLAME, particle.getLastLocation().clone().add(0, 5, 0), 0.4F, 5, 0.4F, 0.05F, 30, ViewDist.LONG);
					}
				}

				if (Math.random() < 0.1)
				{
					particle.getLastLocation().getWorld().playSound(particle.getLastLocation(), Sound.GHAST_FIREBALL, 2, 0.5F);
				}

				for (LivingEntity entity : UtilEnt.getInRadius(particle.getLastLocation(), 2).keySet())
				{
					if (entity.equals(player) || !Recharge.Instance.use(player, GetName() + entity.getUniqueId() + player.getName(), 2000, false, false))
					{
						continue;
					}

					entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.EXPLODE, 2, 0.5F);
					Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, 11,  true, true, false, UtilEnt.getName(player), GetName());
				}
			}
		}, 0, 1);
	}
}
