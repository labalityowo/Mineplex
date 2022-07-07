package mineplex.core.gadget.gadgets.death;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class DeathBalance extends DeathEffectGadget
{

	private static final double DELTA_THETA = Math.PI / 10;

	public DeathBalance(GadgetManager manager)
	{
		super(manager, "Everlasting balance",
				UtilText.splitLineToArray(C.cGray + "Mmmmm Challenge him you must.", LineFormat.LORE),
				CostConstants.FOUND_IN_TREASURE_CHESTS,
				Material.RECORD_9, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);
		event.setItem(Material.RECORD_9, (byte) 0);

		Location locationA = player.getLocation().add(0, 0.1, 0), locationB = locationA.clone();

		Manager.runSyncTimer(new BukkitRunnable()
		{
			double theta = 0, radius = 2, y = 0;

			@Override
			public void run()
			{
				double x = radius * Math.cos(theta), z = radius * Math.sin(theta);

				locationA.add(x, y, z);
				locationB.add(-x, y, -z);

				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, locationA, null, 0, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.SMOKE, locationB, null, 0, 1, ViewDist.NORMAL);

				locationA.subtract(x, y, z);
				locationB.subtract(-x, y, -z);

				theta += DELTA_THETA;
				radius -= 0.1;
				y += 0.2;

				if (radius < 0)
				{
					UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, locationA, null, 0.2F, 5, ViewDist.NORMAL);
					UtilParticle.PlayParticleToAll(ParticleType.SMOKE, locationB, null, 0.5F, 5, ViewDist.NORMAL);
					cancel();
				}
			}
		}, 0, 1);
	}

}