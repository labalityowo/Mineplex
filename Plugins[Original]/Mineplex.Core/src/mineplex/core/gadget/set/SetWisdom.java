package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailEnchant;
import mineplex.core.gadget.gadgets.death.DeathEnchant;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpEnchant;
import mineplex.core.gadget.gadgets.particle.ParticleEnchant;
import mineplex.core.gadget.types.GadgetSet;

public class SetWisdom extends GadgetSet
{

	public SetWisdom(GadgetManager manager)
	{
		super(manager, "Wisdom", "2x Treasure Points while active (Titles)",
				manager.getGadget(ArrowTrailEnchant.class),
				manager.getGadget(DeathEnchant.class),
				manager.getGadget(DoubleJumpEnchant.class),
				manager.getGadget(ParticleEnchant.class));
	}

}
