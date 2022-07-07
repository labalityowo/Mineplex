package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailCandyCane;
import mineplex.core.gadget.gadgets.death.DeathCandyCane;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpCandyCane;
import mineplex.core.gadget.gadgets.particle.ParticleCandyCane;
import mineplex.core.gadget.types.GadgetSet;

public class SetCandyCane extends GadgetSet
{

	public SetCandyCane(GadgetManager manager)
	{
		super(manager, "Candy Cane", "2x Sweet Points while active (Titles)",
				manager.getGadget(ArrowTrailCandyCane.class),
				manager.getGadget(DeathCandyCane.class),
				manager.getGadget(DoubleJumpCandyCane.class),
				manager.getGadget(ParticleCandyCane.class));
	}
}
