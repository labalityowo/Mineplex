package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class DoubleJumpBalance extends DoubleJumpEffectGadget
{

	public DoubleJumpBalance(GadgetManager manager)
	{
		super(manager, "Balanced Leap",
				UtilText.splitLineToArray(C.cGray + "The richest in the world is not the one who has the most, but the one that needs the least.", LineFormat.LORE),
				CostConstants.FOUND_IN_TREASURE_CHESTS,
				Material.RECORD_9, (byte) 0);
	}

	@Override
	public void doEffect(Player player)
	{
		Manager.runSyncTimer(new BukkitRunnable()
		{
			int ticks = 0;

			@Override
			public void run()
			{
				Location location = player.getLocation();

				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, null, 0.05F, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.SMOKE, location, null, 0.05F, 1, ViewDist.NORMAL);

				if (ticks++ > 15)
				{
					cancel();
				}
			}
		}, 0, 1);
	}
}
