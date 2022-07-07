package mineplex.core.gadget.gadgets.kitselector;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.KitSelectorGadget;

public class ShimmeringRingKitSelector extends KitSelectorGadget
{

	private static final int PARTICLES_PER_CIRCLE = 20;

	private double _circleHeight = 0.0;
	private boolean _direction = true;

	public ShimmeringRingKitSelector(GadgetManager manager)
	{
		super(manager, "Shimmering Ring", UtilText.splitLinesToArray(new String[]{C.cGray + "Encaged in your golden rings, your kits weapon still sing."}, LineFormat.LORE),
				0, Material.WOOL, (byte) 4);
	}

	@Override
	public void playParticle(Entity entity, Player playTo)
	{

		// Updates height and direction of particles
		if (_circleHeight <= 0)
		{
			_direction = true;
		}
		else if (_circleHeight >= getEntityHeight(entity))
		{
			_direction = false;
		}
		if (_direction)
			_circleHeight += 0.2;
		else
			_circleHeight -= 0.2;

		for (int i = 0; i < PARTICLES_PER_CIRCLE; i++)
		{
			Location location = entity.getLocation().add(0, _circleHeight, 0);
			double increment = (2 * Math.PI) / PARTICLES_PER_CIRCLE;
			double angle = i * increment;
			Vector vector = new Vector(Math.cos(angle), 0, Math.sin(angle));
			UtilParticle.PlayParticle(UtilParticle.ParticleType.FLAME, location.add(vector), 0, 0, 0, 0, 1, UtilParticle.ViewDist.NORMAL, playTo);
		}
	}

	private double getEntityHeight(Entity entity)
	{
		switch (entity.getType())
		{
			case SHEEP:
			case PIG:
			case BAT:
			case MAGMA_CUBE:
			case GUARDIAN:
			case CHICKEN:
			case SLIME:
			case SQUID:
			case WOLF:
			case OCELOT:
				return 0.75;
			case SPIDER:
			case CAVE_SPIDER:
				return 0.5;
			case ENDERMAN:
				return 3;
		}
		return 2;
	}

}
