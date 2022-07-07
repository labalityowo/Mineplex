package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailConfetti;
import mineplex.core.gadget.gadgets.death.DeathPinataBurst;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpFirecracker;
import mineplex.core.gadget.gadgets.particle.ParticlePartyTime;
import mineplex.core.gadget.types.GadgetSet;

public class SetParty extends GadgetSet
{

	public SetParty(GadgetManager manager)
	{
		super(manager, "Party", "2x Party Points while active (Titles)",
				manager.getGadget(ArrowTrailConfetti.class),
				manager.getGadget(DeathPinataBurst.class),
				manager.getGadget(DoubleJumpFirecracker.class),
				manager.getGadget(ParticlePartyTime.class));
	}

}
