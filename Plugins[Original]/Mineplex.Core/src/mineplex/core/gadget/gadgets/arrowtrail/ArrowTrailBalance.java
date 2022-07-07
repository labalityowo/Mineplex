package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class ArrowTrailBalance extends ArrowEffectGadget
{

	public ArrowTrailBalance(GadgetManager manager)
	{
		super(manager, "Simple Geometry",
				UtilText.splitLineToArray(C.cGray + "If you put water into a tea put, it becomes the tea pot. Be like water my friend.", LineFormat.LORE),
				CostConstants.FOUND_IN_TREASURE_CHESTS,
				Material.RECORD_9, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		Location location = arrow.getLocation();

		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, null, 0, 1, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.SMOKE, location, null, 0, 1, ViewDist.NORMAL);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		Location location = arrow.getLocation();

		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, null, 0.2F, 10, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.SMOKE, location, null, 0.2F, 10, ViewDist.NORMAL);
	}
}