package mineplex.core.gadget.gadgets.item;

import java.awt.*;
import java.time.Month;
import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;

public class ItemMaryPoppins extends ItemGadget
{

	private static final DustSpellColor TOP = new DustSpellColor(Color.BLACK), BOTTOM = new DustSpellColor(new Color(90, 50, 31));

	public ItemMaryPoppins(GadgetManager manager)
	{
		super(manager, "Nanny's Umbrella", new String[]
				{
						C.cGray + "It's Supercarlshardilicouscakewarsaloucous",
						C.cGray + "even though the sound",
						C.cGray + "of it is something quite",
						C.cGray + "atrocious."
				}, CostConstants.POWERPLAY_BONUS, Material.STICK, (byte) 0, TimeUnit.SECONDS.toMillis(20), null);

		Free = false;
		setPPCYearMonth(YearMonth.of(2018, Month.APRIL));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Location location = player.getLocation().add(0, 0.5, 0);

		UtilAction.velocity(player, new Vector(0, 3, 0));
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 0.5F, 0, 0.5F, 0, 10, ViewDist.NORMAL);
		location.getWorld().playSound(location, Sound.BAT_TAKEOFF, 1, 0.6F);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			Chicken chicken;
			int iterations = 0;

			@Override
			public void run()
			{
				if (iterations == 0)
				{
					if (UtilEnt.isGrounded(player))
					{
						cancel();
					}
					else
					{
						Location location = player.getLocation();
						chicken = player.getWorld().spawn(location, Chicken.class);
						chicken.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
						chicken.setPassenger(player);
						UtilEnt.vegetate(chicken, true);
						UtilEnt.ghost(chicken, true, false);
						iterations++;
					}

					return;
				}

				Location location = player.getLocation().add(0, 0.9, 0);
				location.setPitch(0);

				UtilEnt.setPosition(chicken, chicken.getLocation().add(location.getDirection().multiply(0.2)));

				if (iterations % 4 == 0)
				{
					displayUmbrella(location.add(UtilAlg.getRight(location.getDirection()).multiply(0.5)));
				}

				if (!chicken.isValid() || chicken.getPassenger() == null || UtilEnt.isGrounded(chicken))
				{
					chicken.eject();
					chicken.remove();
					cancel();
				}

				iterations++;
			}
		}, 20, 1);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		if (player.getVehicle() != null)
		{
			player.getVehicle().remove();
		}
	}

	private void displayUmbrella(Location location)
	{
		for (double y = 0; y < 1.5; y += 0.1)
		{
			new ColoredParticle(ParticleType.RED_DUST, BOTTOM, location.clone().add(0, y, 0))
					.display();
		}

		location.add(0, 1.5, 0);
		double deltaY = 0;

		for (double r = 0.1; r < 1; r += 0.2)
		{
			for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 10)
			{
				double x = r * Math.cos(theta), z = r * Math.sin(theta);

				location.add(x, deltaY, z);

				new ColoredParticle(ParticleType.RED_DUST, TOP, location)
						.display();

				location.subtract(x, deltaY, z);
			}

			deltaY -= 0.04;
		}
	}
}