package mineplex.core.gadget.gadgets.kitselector;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.KitSelectorGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WaterWingsKitSelector extends KitSelectorGadget
{

	private ShapeWings _wings = new ShapeWings(UtilParticle.ParticleType.DRIP_WATER.particleName, new Vector(0.2,0.2,0.2), 1, 0, false, ShapeWings.DEFAULT_ROTATION, ShapeWings.ANGEL_WING_PATTERN);
	private ShapeWings _wingsEdge = new ShapeWings(UtilParticle.ParticleType.DRIP_WATER.particleName, new Vector(0.1,0.1,0.1), 1, 0, true, ShapeWings.DEFAULT_ROTATION, ShapeWings.ANGEL_WING_PATTERN);

	private int _tick = 0;
	private int _lastTick = 0;

	public WaterWingsKitSelector(GadgetManager manager)
	{
		super(manager, "Water Wings", UtilText.splitLinesToArray(new String[]{C.cGray + "These wings won't help you float or fly, but they look pretty sweet."}, LineFormat.LORE),
				0, Material.WATER_BUCKET, (byte) 0);
	}

	@Override
	public void playParticle(Entity entity, Player playTo)
	{
		if (_tick != _lastTick)
		{
			double offsetY = getEntityYOffset(entity);
			Location loc = entity.getLocation().add(0, offsetY, 0).add(entity.getLocation().getDirection().multiply(-0.2));
			_wings.display(loc, playTo);
			_wingsEdge.display(loc, playTo);
			_lastTick = _tick;
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTEST)
			_tick++;
	}

	/**
	 * Gets the right Y offset for that entity based on the type
	 * @param entity The entity
	 * @return The correct Y offset
	 */
	public double getEntityYOffset(Entity entity)
	{
		EntityType entityType = entity.getType();
		switch (entityType)
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
				return 0.25;
			case ENDERMAN:
				return 1.5;
		}
		return 1.2;
	}

}
