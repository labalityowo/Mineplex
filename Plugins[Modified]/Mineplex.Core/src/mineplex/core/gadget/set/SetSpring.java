package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailSpring;
import mineplex.core.gadget.gadgets.death.DeathSpring;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpSpring;
import mineplex.core.gadget.gadgets.particle.spring.ParticleSpringHalo;
import mineplex.core.gadget.types.GadgetSet;

public class SetSpring extends GadgetSet
{

	public SetSpring(GadgetManager manager)
	{
		super(manager, "Spring", "2x Holiday Points while active (Titles)",
				manager.getGadget(ArrowTrailSpring.class),
				manager.getGadget(DeathSpring.class),
				manager.getGadget(DoubleJumpSpring.class),
				manager.getGadget(ParticleSpringHalo.class));
	}

}
