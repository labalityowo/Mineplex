package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public class SpellImplode extends Spell implements SpellClick
{

	@Override
	public void castSpell(final Player p)
	{
		List<Block> list = p.getLastTwoTargetBlocks(UtilBlock.blockAirFoliageSet, 50);

		if (list.size() > 1)
		{

			Block centerBlock = list.get(0);

			final Location centerLocation = centerBlock.getLocation().clone().add(0.5, 0.5, 0.5);
			final ArrayList<Block> effectedBlocks = new ArrayList<Block>();
			int size = (int) (1.5F + (getSpellLevel(p) * 0.7F));

			for (int x = -size * 2; x <= size * 2; x++)
			{
				for (int y = -size * 2; y <= size * 2; y++)
				{
					for (int z = -size * 2; z <= size * 2; z++)
					{
						Block effectedBlock = centerBlock.getRelative(x, y, z);

						if (effectedBlock.getType() == Material.AIR || effectedBlock.getType() == Material.BEDROCK
								|| effectedBlocks.contains(effectedBlock))
						{
							continue;
						}

						if ((centerLocation.distance(effectedBlock.getLocation().add(0.5, 0.5, 0.5)) + Math.abs(y / 4D))

						<= ((size * 2) + UtilMath.random.nextFloat())

						&& !(effectedBlock.getState() instanceof InventoryHolder))
						{

							effectedBlocks.add(effectedBlock);
						}
					}
				}
			}

			if (effectedBlocks.isEmpty())
			{
				return;
			}

			Collections.shuffle(effectedBlocks);

			new BukkitRunnable()
			{
				int timesRan;
				Iterator<Block> bItel;

				public void run()
				{
					if (effectedBlocks.size() > 0)
					{
						Block block = effectedBlocks.get(UtilMath.r(effectedBlocks.size()));
						block.getWorld().playSound(block.getLocation(),
								new Random().nextBoolean() ? Sound.DIG_GRAVEL : Sound.DIG_GRASS, 2,
								UtilMath.random.nextFloat() / 4);
					}

					if (timesRan % 3 == 0)
					{
						for (int a = 0; a < Math.ceil(effectedBlocks.size() / 3D); a++)
						{
							if (bItel == null || !bItel.hasNext())
							{
								bItel = effectedBlocks.iterator();
							}

							Block block = bItel.next();

							if (block.getType() == Material.AIR)
							{
								continue;
							}

							for (int i = 0; i < 6; i++)
							{
								BlockFace face = BlockFace.values()[i];

								Block b = block.getRelative(face);

								if (UtilBlock.airFoliage(b))
								{
									UtilParticle.PlayParticle(
											ParticleType.BLOCK_CRACK.getParticle(block.getType(), block.getData()),

											block.getLocation().add(

											0.5 + (face.getModX() * 0.6D),

											0.5 + (face.getModY() * 0.6D),

											0.5 + (face.getModZ() * 0.6D)),

											face.getModX() / 2F, face.getModX() / 2F, face.getModX() / 2F, 0, 6,
											ViewDist.LONG, UtilServer.getPlayers());
								}
							}
						}
					}

					if (effectedBlocks.isEmpty())
					{
						cancel();
					}
					else if (timesRan++ >= 20)
					{
						Iterator<Block> itel = effectedBlocks.iterator();

						while (itel.hasNext())
						{
							Block block = itel.next();

							if (block.getType() == Material.AIR || block.getState() instanceof InventoryHolder)
							{
								itel.remove();
								continue;
							}
						}

						Wizards.getArcadeManager().GetExplosion().BlockExplosion(effectedBlocks, centerLocation, false);

						for (Player player : Bukkit.getOnlinePlayers())
						{
							player.playSound(player == p ? p.getLocation() : centerLocation, Sound.ENDERDRAGON_GROWL, 1.5F, 1.5F);
						}

						cancel();
					}
				}
			}.runTaskTimer(Wizards.getArcadeManager().getPlugin(), 0, 0);

			charge(p);
		}
	}
}
