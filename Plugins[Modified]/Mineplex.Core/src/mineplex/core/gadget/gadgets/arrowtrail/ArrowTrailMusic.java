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

public class ArrowTrailMusic extends ArrowEffectGadget
{

	public ArrowTrailMusic(GadgetManager manager)
	{
		super(manager, "Music Arrows", 
				UtilText.splitLineToArray(C.cGray + C.Italics + "\u266B Music is the salve that heals the weary soul. Unlike these. These kill people. \u266B", LineFormat.LORE),
				-2, Material.GREEN_RECORD, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		
		float d = arrow.getTicksLived()%25;
		d /= 24f;
		
		UtilParticle.PlayParticleToAll(ParticleType.NOTE, arrow.getLocation(), d, 0, 0, 1, 0, ViewDist.LONGER);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.NOTE, arrow.getLocation(), 0.5f, 0.3f, 0.5f, 1, 5, ViewDist.LONGER);
	}

}
