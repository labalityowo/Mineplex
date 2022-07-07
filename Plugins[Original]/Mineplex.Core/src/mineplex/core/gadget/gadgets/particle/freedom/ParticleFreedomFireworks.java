package mineplex.core.gadget.gadgets.particle.freedom;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Small fireworks which explode around the player
 */
public class ParticleFreedomFireworks extends ParticleGadget
{
	/** Block types to source for particles */
	private static final Material[] BLOCKTYPES = {
			Material.REDSTONE_BLOCK,
			Material.LAPIS_BLOCK,
			Material.QUARTZ_BLOCK
	};

	/** Amount of particles for each firework */
	private static final int PARTICLE_COUNT = 20;

	public ParticleFreedomFireworks(GadgetManager manager)
	{
		super(manager, "Freedom Fireworks",
				UtilText.splitLineToArray(C.cGray + "Keep your patriotism close.", LineFormat.LORE),
				-8, Material.WOOL, (byte) 0);

		setDisplayItem(CountryFlag.USA.getBanner());
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		String particle = UtilParticle.ParticleType.BLOCK_CRACK.getParticle(BLOCKTYPES[ThreadLocalRandom.current().nextInt(0, BLOCKTYPES.length)], 0);
		Location location = UtilMath.gauss(player.getEyeLocation(), 1, 1, 1);

		for (int i = 0; i < PARTICLE_COUNT; ++i)
		{
			UtilParticle.PlayParticleToAll(particle, location, null, 3.0f, 1, UtilParticle.ViewDist.NORMAL);
		}
	}
}
