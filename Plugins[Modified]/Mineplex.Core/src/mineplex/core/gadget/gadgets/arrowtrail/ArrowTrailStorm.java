package mineplex.core.gadget.gadgets.arrowtrail;

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

public class ArrowTrailStorm extends ArrowEffectGadget
{

	public ArrowTrailStorm(GadgetManager manager)
	{
		super(manager, "Rain Arrows", 
				UtilText.splitLineToArray(C.cGray + "Arrows that bless the rain down in Africa.", LineFormat.LORE),
				-2, Material.INK_SACK, (byte) 4);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.SPLASH, arrow.getLocation(), 0.0f, 0.0f, 0.0f, 0.3f, 10, ViewDist.LONGER);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.SPLASH, arrow.getLocation(), 0.4f, 0.4f, 0.4f, 0.7f, 160, ViewDist.LONGER);
	}

}
