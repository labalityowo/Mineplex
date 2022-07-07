package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailBlood;
import mineplex.core.gadget.gadgets.death.DeathBlood;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpBlood;
import mineplex.core.gadget.gadgets.particle.ParticleBlood;
import mineplex.core.gadget.types.GadgetSet;

public class SetVampire extends GadgetSet
{

	public SetVampire(GadgetManager manager)
	{
		super(manager, "Blood", "2x Warrior Points while active (Titles)",
				manager.getGadget(ArrowTrailBlood.class),
				manager.getGadget(DeathBlood.class),
				manager.getGadget(DoubleJumpBlood.class),
				manager.getGadget(ParticleBlood.class));
	}

}
