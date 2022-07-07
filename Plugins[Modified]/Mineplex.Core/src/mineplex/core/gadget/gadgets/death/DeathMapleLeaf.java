package mineplex.core.gadget.gadgets.death;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Displays a giant maple leaf at the point of death.
 */
public class DeathMapleLeaf extends DeathEffectGadget
{
	/** height off the ground of the leaf */
	private static final double HEIGHT = 3;

	private final ShapeWings _leafOuter = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1.0, 1.0, 1.0), 1, 0, false, 0, ShapeWings.MAPLE_LEAF);
	private final ShapeWings _leafInner = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(0.7, 0, 0), 1, 0, false, 0, ShapeWings.MAPLE_LEAF);

	public DeathMapleLeaf(GadgetManager manager)
	{
		super(manager, "Fallen Maple Leaf",
				UtilText.splitLineToArray(C.cGray + "When you die in " + C.cRed + "Canada" + C.cGray + " you die in real life.", LineFormat.LORE),
				-8, Material.WOOL, (byte) 0);

		setDisplayItem(CountryFlag.CANADA.getBanner());
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);
		Location loc = player.getLocation().add(0, HEIGHT, 0);
		_leafOuter.display(loc);
		_leafInner.display(loc);
	}
}
