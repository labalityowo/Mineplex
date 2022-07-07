package mineplex.core.gadget.gadgets.particle.freedom;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.awt.Color;

/**
 * Leaves a trail behind the player with the colors of the Canadian flag.
 */
public class ParticleCanadian extends ParticleGadget
{
	/** # of lines of particles */
	private static final int STRAND_COUNT = 9;

	/** How far apart each line of particles is */
	private static final double STRAND_SPACING = 0.124;

	/** How far off the floor the particles begin */
	private static final double DISTANCE_FROM_FLOOR = 0.43;

	public ParticleCanadian(GadgetManager manager)
	{
		super(manager, "Canadian Trail",
				UtilText.splitLineToArray(C.cGray + "Lead the way to freedom!", LineFormat.LORE),
				-8, Material.WOOL, (byte) 0);

		setDisplayItem(CountryFlag.CANADA.getBanner());
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Location center = player.getLocation().subtract(player.getLocation().getDirection().multiply(0.4))
				.subtract(0, 0.1, 0).add(0, DISTANCE_FROM_FLOOR, 0);

		if (Manager.isMoving(player))
		{
			for (int i = 0; i < STRAND_COUNT; i++)
			{
				if (i < 3 || i > 5)
				{
					UtilParticle.playColoredParticleToAll(Color.RED, UtilParticle.ParticleType.RED_DUST,
							center.add(0, STRAND_SPACING, 0), 0, UtilParticle.ViewDist.NORMAL);
				}
				else
				{
					UtilParticle.playColoredParticleToAll(Color.WHITE, UtilParticle.ParticleType.RED_DUST,
							center.add(0, STRAND_SPACING, 0), 0, UtilParticle.ViewDist.NORMAL);
				}
			}
		}
	}
}
