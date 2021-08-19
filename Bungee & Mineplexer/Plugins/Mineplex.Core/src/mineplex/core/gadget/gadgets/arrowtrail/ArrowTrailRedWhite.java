package mineplex.core.gadget.gadgets.arrowtrail;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

import java.awt.Color;

/**
 * Trails a red and white double helix behind the arrow.
 */
public class ArrowTrailRedWhite extends ArrowEffectGadget
{
	public ArrowTrailRedWhite(GadgetManager manager)
	{
		super(manager, "Red & White Arrows",
				UtilText.splitLineToArray(C.cRed + "Killing you nicely.", LineFormat.LORE),
				-8, Material.WOOL, (byte)0);

		setDisplayItem(CountryFlag.CANADA.getBanner());
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		Vector v = arrow.getVelocity();
		Vector up = UtilAlg.getUp(v);
		Vector left = UtilAlg.getLeft(v);

		Location loc = arrow.getLocation();

		double amount = 2;
		double ticks = 15;

		for(int i = 0; i < amount; i++)
		{
			double rad = Math.PI*2.0;
			rad += i/amount * rad;
			rad += Math.PI*2*(arrow.getTicksLived()%ticks)/ticks;
			double l = -Math.sin(rad);
			double u = Math.cos(rad);

			Vector vel = v.clone().add(up.clone().multiply(u)).add(left.clone().multiply(l));
			vel.multiply(0.4);

			if (i == 0)
			{
				for(int j = 0; j < 3; ++j)
				{
					UtilParticle.playColoredParticleToAll(Color.RED, UtilParticle.ParticleType.RED_DUST, loc.clone().add(vel), 0, UtilParticle.ViewDist.NORMAL);
				}
			}
			else
			{
				for(int j = 0; j < 3; ++j)
				{
					UtilParticle.playColoredParticleToAll(Color.WHITE, UtilParticle.ParticleType.RED_DUST, loc.clone().add(vel), 0, UtilParticle.ViewDist.NORMAL);
				}
			}
		}
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.EXPLODE, arrow.getLocation(), 0, 0, 0, 0, 3, UtilParticle.ViewDist.NORMAL);
	}
}
