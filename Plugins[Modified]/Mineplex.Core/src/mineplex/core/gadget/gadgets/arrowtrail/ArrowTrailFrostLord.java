package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.gadget.GadgetManager;

public class ArrowTrailFrostLord extends ArrowEffectGadget
{
	public ArrowTrailFrostLord(GadgetManager manager)
	{
		super(manager, "Arrows of the Frost Lord", 
				UtilText.splitLineToArray(C.cGray + "The Frost Lord's arrows bring a blast of winter in the wind of their passing.", LineFormat.LORE),
				-3,
				Material.SNOW_BALL, (byte)0, "Frost Lord");
	}
	
	@Override
	public void doTrail(Arrow arrow)
	{
		UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, arrow.getLocation(), 0f, 0f, 0f, 0f, 1,
				ViewDist.LONGER, UtilServer.getPlayers());
	}
	
	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, arrow.getLocation(), 0f, 0f, 0f, 0.4f, 12,
				ViewDist.LONGER, UtilServer.getPlayers());
	}
}
