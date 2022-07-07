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

public class ArrowTrailConfetti extends ArrowEffectGadget
{
	
	private byte[] _data = new byte[]{1,2,4,5,6,9,10,11,12,13,14,15};

	public ArrowTrailConfetti(GadgetManager manager)
	{
		super(manager, "Arrow Confetti", 
				UtilText.splitLineToArray(C.cGray + "This " + C.cPurple + "party train" + C.cGray + " won't stop till the arrow hits your face.", LineFormat.LORE),
				-2, Material.FIREWORK, (byte)0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		String particle = ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, _data[arrow.getTicksLived()%_data.length]);
		UtilParticle.PlayParticleToAll(particle, arrow.getLocation(), null, 0, 3, ViewDist.LONGER);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		for(byte data : _data) {
			String particle = ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, data);
			UtilParticle.PlayParticleToAll(particle, arrow.getLocation(), null, 0.2f, 5, ViewDist.LONGER);
		}
	}

}
