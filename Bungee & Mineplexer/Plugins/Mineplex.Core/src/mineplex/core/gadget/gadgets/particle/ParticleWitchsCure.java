package mineplex.core.gadget.gadgets.particle;

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

public class ParticleWitchsCure extends ParticleGadget
{

	private static final double RADIUS = 1.8;
	private static final double DELTA_THETA = Math.PI / 30;
	private static final double DELTA_THETA_2 = Math.PI / 2;

	private double _theta;

	public ParticleWitchsCure(GadgetManager manager)
	{
		super(manager, "Witch's Curse", UtilText.splitLineToArray(C.cGray + "A said I put a curse on yee.", LineFormat.LORE), CostConstants.LEVEL_REWARDS, Material.SPIDER_EYE, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location location = player.getLocation().add(0, 0.7, 0);

		if (Manager.isMoving(player))
		{
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, location, null, 0, 1, ViewDist.NORMAL);
		}
		else
		{
			for (double theta = 0; theta < 2 * Math.PI; theta += DELTA_THETA_2)
			{
				double sin = Math.sin(theta + _theta);
				double x = RADIUS * Math.cos(theta + _theta), z = RADIUS * sin;

				sin /= 4;

				location.add(x, sin, z);

				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, location, null, 0, 1, ViewDist.NORMAL);

				location.subtract(x, sin, z);
			}
		}
	}

	@Override
	@EventHandler
	public void Caller(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_theta += DELTA_THETA;
		super.Caller(event);
	}
}
