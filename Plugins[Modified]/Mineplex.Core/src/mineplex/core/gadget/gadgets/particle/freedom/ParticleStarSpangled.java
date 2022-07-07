package mineplex.core.gadget.gadgets.particle.freedom;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.awt.*;


/**
 * @author J Teissler
 * @date 6/26/17
 */
public class ParticleStarSpangled extends ParticleGadget
{
	private static final int STRAND_COUNT = 9;
	private static final double STRAND_SPACING = 0.124;
	private static final double DISTANCE_FROM_FLOOR = 0.43;

	private static final Color BLUE = new Color(29, 26, 120);

	public ParticleStarSpangled(GadgetManager manager)
	{
		super(manager, "Star Spangled Stripe",
				UtilText.splitLineToArray(C.cGray + "Blaze a trail of freedom!", LineFormat.LORE),
				-8, Material.WOOL, (byte) 0);
		setDisplayItem(CountryFlag.USA.getBanner());
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
				if (i == 3 || i == 7)
				{
					if (player.getTicksLived() % 3 == 0)
					{
						UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.FIREWORKS_SPARK,
								center.add(0, STRAND_SPACING, 0), 0f, 0f, 0f, 0f, 0, UtilParticle.ViewDist.NORMAL);
						continue;
					}
				}
				else if (i == 5)
				{
					if (player.getTicksLived() + 1 % 3 == 0)
					{
						UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.FIREWORKS_SPARK,
								center.add(0, STRAND_SPACING, 0), 0f, 0f, 0f, 0f, 0, UtilParticle.ViewDist.NORMAL);
						continue;
					}
				}

				UtilParticle.playColoredParticleToAll(BLUE, UtilParticle.ParticleType.RED_DUST,
						center.add(0, STRAND_SPACING, 0), 0, UtilParticle.ViewDist.NORMAL);
			}
		}
	}
}
