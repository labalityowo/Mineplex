package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailTitan;
import mineplex.core.gadget.gadgets.death.DeathTitan;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpTitan;
import mineplex.core.gadget.gadgets.particle.ParticleTitan;
import mineplex.core.gadget.types.GadgetSet;

public class SetTitan extends GadgetSet
{

	public SetTitan(GadgetManager manager)
	{
		super(manager, "The Titans", "Improved Flame of the Titans",
				manager.getGadget(ArrowTrailTitan.class),
				manager.getGadget(DeathTitan.class),
				manager.getGadget(DoubleJumpTitan.class),
				manager.getGadget(ParticleTitan.class));
	}
}
