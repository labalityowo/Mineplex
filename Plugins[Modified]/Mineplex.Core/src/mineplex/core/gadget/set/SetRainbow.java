package mineplex.core.gadget.set;

import org.bukkit.Color;

import mineplex.core.common.util.C;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailRainbow;
import mineplex.core.gadget.gadgets.death.DeathRainbow;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpRainbow;
import mineplex.core.gadget.gadgets.particle.ParticleRainbow;
import mineplex.core.gadget.types.GadgetSet;

public class SetRainbow extends GadgetSet
{

	public static final int PER_LEVEL = 10;
	public static final String[] GADGET_LORE =
			{
					C.cWhite + "Bask in the light of",
					C.cWhite + "your dedication with a",
					C.cWhite + "set of rainbows that get",
					C.cWhite + "cooler the more you level up",
					C.blankLine,
					C.cWhite + "+1 Color per " + PER_LEVEL + " Mineplex Levels",
			};
	public static final DustSpellColor[] COLOURS =
			{
					new DustSpellColor(Color.RED),
					new DustSpellColor(Color.ORANGE),
					new DustSpellColor(Color.YELLOW),
					new DustSpellColor(Color.LIME),
					new DustSpellColor(Color.GREEN),
					new DustSpellColor(Color.AQUA),
					new DustSpellColor(Color.TEAL),
					new DustSpellColor(Color.BLUE),
					new DustSpellColor(Color.FUCHSIA),
					new DustSpellColor(Color.PURPLE),
			};

	public SetRainbow(GadgetManager manager)
	{
		super(manager, "Rainbow", "None",
				manager.getGadget(ArrowTrailRainbow.class),
				manager.getGadget(DeathRainbow.class),
				manager.getGadget(DoubleJumpRainbow.class),
				manager.getGadget(ParticleRainbow.class)
		);
	}
}
