package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;

public class ArrowTrailCupid extends ArrowEffectGadget
{

	public ArrowTrailCupid(GadgetManager manager)
	{
		super(manager, "Cupid's Arrows", 
				UtilText.splitLineToArray("That’s the power of love!", LineFormat.LORE),
				-2, Material.APPLE, (byte)0, "Arrows of Cupid");
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		Vector color = arrow.getTicksLived()%2 == 0? UtilColor.colorToVector(Color.RED) : UtilColor.colorToVector(Color.fromRGB(16738740));
		UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, arrow.getLocation(), color, 1, 0, ViewDist.LONG);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.HEART, arrow.getLocation(), 0.4f, 0, 0.4f, 0.5f, 4, ViewDist.LONG);
	}

}
