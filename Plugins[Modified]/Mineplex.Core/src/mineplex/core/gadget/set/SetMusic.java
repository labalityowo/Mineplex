package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailMusic;
import mineplex.core.gadget.gadgets.death.DeathMusic;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpMusic;
import mineplex.core.gadget.gadgets.particle.ParticleMusic;
import mineplex.core.gadget.types.GadgetSet;

public class SetMusic extends GadgetSet
{

	public SetMusic(GadgetManager manager)
	{
		super(manager, "Ultimate Music Collection", "2x Peaceful Points while active (Titles)",
				manager.getGadget(ArrowTrailMusic.class),
				manager.getGadget(DeathMusic.class),
				manager.getGadget(DoubleJumpMusic.class),
				manager.getGadget(ParticleMusic.class));
	}

}
