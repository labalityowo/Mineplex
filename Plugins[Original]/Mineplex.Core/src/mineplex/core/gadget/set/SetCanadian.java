package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailRedWhite;
import mineplex.core.gadget.gadgets.death.DeathMapleLeaf;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpMaple;
import mineplex.core.gadget.gadgets.particle.freedom.ParticleCanadian;
import mineplex.core.gadget.types.GadgetSet;

public class SetCanadian extends GadgetSet
{

	public SetCanadian(GadgetManager manager)
	{
		super(manager, "Canadian", "2x Holiday Points while active (Titles)",
				manager.getGadget(ArrowTrailRedWhite.class),
				manager.getGadget(DeathMapleLeaf.class),
				manager.getGadget(DoubleJumpMaple.class),
				manager.getGadget(ParticleCanadian.class));
	}

}
