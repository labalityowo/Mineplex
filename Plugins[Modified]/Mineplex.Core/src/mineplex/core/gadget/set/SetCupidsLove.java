package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailCupid;
import mineplex.core.gadget.gadgets.death.DeathCupidsBrokenHeart;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpCupidsWings;
import mineplex.core.gadget.gadgets.particle.ParticleHeart;
import mineplex.core.gadget.types.GadgetSet;

public class SetCupidsLove extends GadgetSet
{

	public SetCupidsLove(GadgetManager manager)
	{
		super(manager, "Cupid's Love", "2x Holiday Points while active (Titles)",
				manager.getGadget(ArrowTrailCupid.class),
				manager.getGadget(DeathCupidsBrokenHeart.class),
				manager.getGadget(DoubleJumpCupidsWings.class),
				manager.getGadget(ParticleHeart.class));
	}

}
