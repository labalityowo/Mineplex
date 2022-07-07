package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailFreedom;
import mineplex.core.gadget.gadgets.death.DeathFreedom;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFreedom;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleStarSpangled;
import mineplex.core.gadget.types.GadgetSet;

public class SetFreedom extends GadgetSet
{

	public SetFreedom(GadgetManager manager)
	{
		super(manager, "Freedom", "2x Holiday Points while active (Titles)",
				manager.getGadget(ArrowTrailFreedom.class),
				manager.getGadget(DeathFreedom.class),
				manager.getGadget(DoubleJumpFreedom.class),
				manager.getGadget(ParticleStarSpangled.class));
	}

}
