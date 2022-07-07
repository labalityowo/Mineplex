package mineplex.core.gadget.gadgets.arrowtrail;

import mineplex.core.common.util.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;

public class ArrowTrailEmerald extends ArrowEffectGadget
{

	public ArrowTrailEmerald(GadgetManager manager)
	{
		super(manager, "Green Arrows",
				UtilText.splitLineToArray(C.cGreen + "The arrows of a hero long past. Some say he hung out with bats and canaries.", LineFormat.LORE),
				-2, Material.EMERALD, (byte)0);
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

			UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, loc.clone().add(vel), vel, 1f, 0, ViewDist.LONGER);
		}
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, arrow.getLocation(), 0.35f, 0.35f, 0.35f, 1f, 15, ViewDist.LONGER);
	}

}
