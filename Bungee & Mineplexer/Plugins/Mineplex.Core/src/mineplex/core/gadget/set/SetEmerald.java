package mineplex.core.gadget.set;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailEmerald;
import mineplex.core.gadget.gadgets.death.DeathEmerald;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpEmerald;
import mineplex.core.gadget.gadgets.particle.ParticleEmerald;
import mineplex.core.gadget.types.GadgetSet;

public class SetEmerald extends GadgetSet
{

	public SetEmerald(GadgetManager manager)
	{
		super(manager, "Emerald", "2x Gem Points while active (Titles)",
				manager.getGadget(ArrowTrailEmerald.class),
				manager.getGadget(DeathEmerald.class),
				manager.getGadget(DoubleJumpEmerald.class),
				manager.getGadget(ParticleEmerald.class));
	}

}
