package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetLegend;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class ArrowTrailLegend extends ArrowEffectGadget
{

	public ArrowTrailLegend(GadgetManager manager)
	{
		super(manager, "Arrows of the Legends",
				UtilText.splitLineToArray(C.cGray + "Pwaaaaa Legendary!", LineFormat.LORE),
				CostConstants.UNLOCKED_WITH_LEGEND,
				Material.ENDER_PORTAL_FRAME, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		Location location = arrow.getLocation();

		for (DustSpellColor color : SetLegend.SELECTABLE_COLORS)
		{
			new ColoredParticle(ParticleType.RED_DUST, color, location)
					.display();
		}
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		for (DustSpellColor color : SetLegend.SELECTABLE_COLORS)
		{
			Location location = UtilAlg.getRandomLocation(arrow.getLocation(), 0.8, 0.3, 0.8);
			int i = 0;

			while (i++ < 15)
			{
				new ColoredParticle(ParticleType.RED_DUST, color, location.add(0, 0.1, 0))
						.display();
			}
		}
	}

}