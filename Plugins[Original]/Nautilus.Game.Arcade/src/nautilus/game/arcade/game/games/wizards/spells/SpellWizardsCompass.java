package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpellWizardsCompass extends Spell implements SpellClick
{

	@Override
	public void castSpell(Player p)
	{
		Location loc = p.getEyeLocation().subtract(0, 1, 0);

		final ArrayList<Integer[]> colors = new ArrayList<Integer[]>();

		for (int x = -1; x <= 1; x++)
		{

			for (int y = -1; y <= 1; y++)
			{

				for (int z = -1; z <= 1; z++)
				{
					colors.add(new Integer[]
						{
								x, y, z
						});
				}
			}
		}

		Collections.shuffle(colors);

		for (Player enemy : Wizards.GetPlayers(true))
		{
			if (enemy == p)
			{
				continue;
			}

			final double playerDist = Math.min(7, UtilMath.offset(enemy, p));

			final Vector traj = UtilAlg.getTrajectory(p.getLocation(), enemy.getEyeLocation()).multiply(0.1);

			final Hologram hologram = new Hologram(Wizards.getArcadeManager().getHologramManager(), loc.clone().add(0, 0.3, 0)
					.add(traj.clone().normalize().multiply(playerDist)), enemy.getName());

			hologram.setHologramTarget(HologramTarget.WHITELIST);
			hologram.addPlayer(p);

			hologram.start();

			final Location location = loc.clone();
			final Integer[] ints = colors.remove(0);

			new BukkitRunnable()
			{
				int dist;
				int tick;
				HashMap<Location, Integer> locations = new HashMap<Location, Integer>();

				public void run()
				{
					tick++;

					Iterator<Entry<Location, Integer>> itel = locations.entrySet().iterator();

					while (itel.hasNext())
					{
						Entry<Location, Integer> entry = itel.next();

						if ((entry.getValue() + tick) % 3 == 0)
						{
							// Colored redstone dust
							UtilParticle.PlayParticle(ParticleType.RED_DUST, entry.getKey(), ints[0], ints[1], ints[2], 1, 0,
									ViewDist.LONG, UtilServer.getPlayers());
						}

						if (entry.getValue() < tick)
						{
							itel.remove();
						}
					}

					if (dist <= playerDist * 10)
					{
						for (int a = 0; a < 2; a++)
						{
							// Colored redstone dust
							UtilParticle.PlayParticle(ParticleType.RED_DUST, location, ints[0], ints[1], ints[2], 1, 0,
									ViewDist.LONG, UtilServer.getPlayers());

							locations.put(location.clone(), tick + 50);

							location.add(traj);
							dist++;
						}
					}
					else if (locations.isEmpty())
					{
						hologram.stop();
						cancel();
					}
				}
			}.runTaskTimer(Wizards.getArcadeManager().getPlugin(), 0, 0);
		}

		p.playSound(p.getLocation(), Sound.ZOMBIE_UNFECT, 1.5F, 1);

		charge(p);
	}
}
