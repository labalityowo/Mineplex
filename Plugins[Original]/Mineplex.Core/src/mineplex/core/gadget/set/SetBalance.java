package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailBalance;
import mineplex.core.gadget.gadgets.death.DeathBalance;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpBalance;
import mineplex.core.gadget.gadgets.particle.ParticleYinYang;
import mineplex.core.gadget.types.GadgetSet;

public class SetBalance extends GadgetSet
{

	public SetBalance(GadgetManager manager)
	{
		super(manager, "Balance", "Rainbow Yin Yang",
				manager.getGadget(ArrowTrailBalance.class),
				manager.getGadget(DeathBalance.class),
				manager.getGadget(DoubleJumpBalance.class),
				manager.getGadget(ParticleYinYang.class)
		);
	}


}
