package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetLegend;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class DoubleJumpLegend extends DoubleJumpEffectGadget
{

	public DoubleJumpLegend(GadgetManager manager)
	{
		super(manager, "Legendary Leap",
				UtilText.splitLineToArray(C.cGray + "Reach the height of legends.", LineFormat.LORE),
				CostConstants.UNLOCKED_WITH_LEGEND,
				Material.ENDER_PORTAL_FRAME, (byte) 0);
	}

	@Override
	public void doEffect(Player player)
	{
		int i = 0;

		Location center = player.getLocation().add(0, 0.3, 0);

		for (Location location : UtilShapes.getPointsInCircle(center, 50, 1))
		{
			new ColoredParticle(ParticleType.RED_DUST, SetLegend.SELECTABLE_COLORS[i++ % SetLegend.SELECTABLE_COLORS.length], location)
					.display();
		}
		center.getWorld().playEffect(center, Effect.ENDER_SIGNAL, 0);
	}

}