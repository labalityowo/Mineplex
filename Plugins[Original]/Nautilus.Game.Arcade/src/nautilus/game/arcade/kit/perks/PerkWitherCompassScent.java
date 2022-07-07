package nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PerkWitherCompassScent extends Perk
{
	public PerkWitherCompassScent()
	{
		super("Smell Humans", new String[]
				{
				C.cYellow + "Right-Click" + C.cGray + " with a compass to use " + C.cGreen + "Wither Scent"
				});
	}
	
	@EventHandler
	public void compassRightClick(PlayerInteractEvent event)
	{
		if(event.isCancelled())
			return;
		
		if(!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.COMPASS))
			return;

		if(!Kit.HasKit(event.getPlayer()))
			return;
		
		if (!Recharge.Instance.use(event.getPlayer(), GetName(), 30*1000, true, true))
			return;
		
		event.setCancelled(true);
		Player player = event.getPlayer();
		
		// Code from Wizards changed to WitherGame:
		Location loc = player.getEyeLocation().subtract(0, 1, 0);

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

		for (Player enemy : Manager.GetGame().GetPlayers(true))
		{
			if (enemy == player)
			{
				continue;
			}
			
			if(Manager.GetGame().GetTeam(enemy).GetName().contentEquals("Withers"))
			{
				continue;
			}

			final double playerDist = Math.min(7, UtilMath.offset(enemy, player));

			final Vector traj = UtilAlg.getTrajectory(player.getLocation(), enemy.getEyeLocation()).multiply(0.1);

			final Hologram hologram = new Hologram(Manager.GetGame().getArcadeManager().getHologramManager(), loc.clone().add(0, 0.3, 0)
					.add(traj.clone().normalize().multiply(playerDist)), enemy.getName());

			hologram.setHologramTarget(HologramTarget.WHITELIST);
			hologram.addPlayer(player);

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
			}.runTaskTimer(Manager.GetGame().getArcadeManager().getPlugin(), 0, 0);
		}

		player.playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 1.5F, 1);

	}
}
