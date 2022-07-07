package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class SpellLightningStrike extends Spell implements SpellClick
{

	@Override
	public void castSpell(final Player p)
	{
		double curRange = 0;

		while (curRange <= 150)
		{
			Location newTarget = p.getEyeLocation().add(new Vector(0, 0.2, 0))
					.add(p.getLocation().getDirection().multiply(curRange));

			if (!UtilBlock.airFoliage(newTarget.getBlock())
					|| !UtilBlock.airFoliage(newTarget.getBlock().getRelative(BlockFace.UP)))
				break;

			// Progress Forwards
			curRange += 0.2;
		}

		if (curRange < 2)
		{
			return;
		}

		// Destination
		final Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(curRange).add(new Vector(0, 0.4, 0)));

		while (UtilBlock.solid(loc.getBlock().getRelative(BlockFace.UP)))
		{
			loc.add(0, 1, 0);
		}

		UtilParticle.PlayParticle(ParticleType.ANGRY_VILLAGER, loc.clone().add(0, 1.3, 0), 0.5F, 0.3F, 0.5F, 0, 7,
				ViewDist.LONG, UtilServer.getPlayers());

		Bukkit.getScheduler().scheduleSyncDelayedTask(Wizards.getArcadeManager().getPlugin(), new Runnable()
		{

			@Override
			public void run()
			{
				LightningStrike lightning = p.getWorld().strikeLightning(loc);

				lightning.setMetadata("Damager", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), p));

				Block b = loc.getWorld().getHighestBlockAt(loc);

				b = b.getRelative(BlockFace.DOWN);

				ArrayList<Block> toExplode = new ArrayList<Block>();
				ArrayList<Block> toFire = new ArrayList<Block>();

				for (int x = -1; x <= 1; x++)
				{
					for (int y = -1; y <= 1; y++)
					{
						for (int z = -1; z <= 1; z++)
						{
							if (x == 0 || (Math.abs(x) != Math.abs(z) || UtilMath.r(3) == 0))
							{
								Block block = b.getRelative(x, y, z);

								if ((y == 0 || (x == 0 && z == 0)) && block.getType() != Material.AIR
										&& block.getType() != Material.BEDROCK)
								{
									if (y == 0 || UtilMath.random.nextBoolean())
									{
										toExplode.add(block);
										toFire.add(block);
									}
								}
								else if (block.getType() == Material.AIR)
								{
									toFire.add(block);
								}
							}
						}
					}
				}

				Wizards.getArcadeManager().GetExplosion().BlockExplosion(toExplode, b.getLocation(), false);

				for (Block block : toFire)
				{
					if (UtilMath.random.nextBoolean())
					{
						block.setType(Material.FIRE);
					}
				}
			}

		}, 20);

		charge(p);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof LightningStrike && event.getEntity() instanceof LivingEntity)
		{
			LightningStrike lightning = (LightningStrike) event.getDamager();

			if (lightning.hasMetadata("Damager"))
			{
				event.setCancelled(true);

				if (!lightning.hasMetadata("IgnoreDamage"))
				{
					lightning.setMetadata("IgnoreDamage", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), null));

					event.getEntity().setFireTicks(80);

					Player player = (Player) lightning.getMetadata("Damager").get(0).value();

					Wizards.getArcadeManager()
							.GetDamage()
							.NewDamageEvent((LivingEntity) event.getEntity(), player, null, DamageCause.LIGHTNING,
									2 + (4 * getSpellLevel(player)), false, true, false, "Lightning Strike", "Lightning Strike");
				}
			}
		}
	}

}
