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

public class ArrowTrailBlood extends ArrowEffectGadget
{

	public ArrowTrailBlood(GadgetManager manager)
	{
		super(manager, "Bloody Arrows", 
				UtilText.splitLineToArray(C.cGray + "Arrows soaked in blood. No effect, but your enemies may fear getting icky right before dying.", LineFormat.LORE),
				-2, Material.REDSTONE, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, arrow.getLocation(), null, 0f, 1, ViewDist.LONG);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.REDSTONE_BLOCK, 0), arrow.getLocation(), null, 0.05f, 3, ViewDist.LONG);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, arrow.getLocation(), 0.4f, 0.4f, 0.4f, 0f, 20, ViewDist.LONG);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.REDSTONE_BLOCK, 0), arrow.getLocation(), 0.1f, 0.1f, 0.1f, 0.25f, 60, ViewDist.LONG);
		
	}

}
