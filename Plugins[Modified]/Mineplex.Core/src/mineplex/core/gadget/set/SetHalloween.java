package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailHalloween;
import mineplex.core.gadget.gadgets.death.DeathHalloween;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpHalloween;
import mineplex.core.gadget.gadgets.particle.ParticleHalloween;
import mineplex.core.gadget.types.GadgetSet;

public class SetHalloween extends GadgetSet
{

	public SetHalloween(GadgetManager manager)
	{
		super(manager, "Spooky", "Increased chance of spawning Flaming Pumpkins in game (During Halloween). 2x Holiday Points while active (Titles)",
				manager.getGadget(ArrowTrailHalloween.class),
				manager.getGadget(DeathHalloween.class),
				manager.getGadget(DoubleJumpHalloween.class),
				manager.getGadget(ParticleHalloween.class)
		);
	}
}
