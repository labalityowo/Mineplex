package mineplex.core.gadget.gadgets.particle.christmas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleBlizzard extends ParticleGadget
{

	private static final double DELTA_THETA = Math.PI / 12;
	private static final double RADIUS = 0.7;
	private static final double HEIGHT = 1.9;

	private double _theta;

	public ParticleBlizzard(GadgetManager manager)
	{
		super(manager, "Blizzard Aura",
				UtilText.splitLineToArray(C.cGray + "How did this much snow get through?", LineFormat.LORE),
				CostConstants.FOUND_IN_GINGERBREAD_CHESTS, Material.ICE, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location location = player.getLocation();

		if (Manager.isMoving(player))
		{
			UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location.add(0, 1 + Math.sin(_theta), 0), null, 0, 1, ViewDist.NORMAL);
			return;
		}

		location.add(0, HEIGHT, 0);
		double x = RADIUS * Math.cos(_theta);
		double z = RADIUS * Math.sin(_theta);

		location.add(x, 0, z);

		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, null, 0, 1, ViewDist.NORMAL);

		location.subtract(x * 2, 0, z * 2);

		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, null, 0, 1, ViewDist.NORMAL);

		UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, location.subtract(0, HEIGHT - 0.8, 0), 0.7F, 0.4F, 0.7F, 0, 3, ViewDist.NORMAL);
	}

	@Override
	@EventHandler
	public void Caller(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		super.Caller(event);
		_theta += DELTA_THETA;
	}
}
