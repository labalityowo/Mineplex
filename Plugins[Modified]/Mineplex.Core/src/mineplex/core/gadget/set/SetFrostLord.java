package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailFrostLord;
import mineplex.core.gadget.gadgets.death.DeathFrostLord;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFrostLord;
import mineplex.core.gadget.gadgets.particle.ParticleFrostLord;
import mineplex.core.gadget.types.GadgetSet;

public class SetFrostLord extends GadgetSet
{

	public SetFrostLord(GadgetManager manager)
	{
		super(manager, "Frost Lord", "2x Holiday Points while active (Titles)",
				manager.getGadget(ArrowTrailFrostLord.class),
				manager.getGadget(DeathFrostLord.class),
				manager.getGadget(DoubleJumpFrostLord.class),
				manager.getGadget(ParticleFrostLord.class));
	}
}
