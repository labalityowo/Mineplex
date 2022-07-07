package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleInfused extends ParticleGadget
{

	private static final double DELTA_THETA = Math.PI / 20;
	private static final double DELTA_Y = 0.05;
	private static final double RADIUS = 1.4;
	private static final double RADIUS_MOVING = 0.4;
	private static final double MAX_HEIGHT = 2.5;
	private static final DustSpellColor RED = new DustSpellColor(Color.RED);
	private static final DustSpellColor BLUE = new DustSpellColor(Color.AQUA);

	private double _theta, _y, _radius;
	private boolean _up;

	public ParticleInfused(GadgetManager manager)
	{
		super(manager, "Colliding Colors", UtilText.splitLineToArray(C.cGray + "The legendary mage Azmah spent years trying to infuse these two super powers.", LineFormat.LORE), CostConstants.FOUND_IN_TREASURE_CHESTS, Material.INK_SACK, (byte) 12);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location locationA, locationB;

		if (Manager.isMoving(player))
		{
			locationA = player.getLocation().add(0, 1, 0);
			locationB = locationA.clone();
			Location fixed = locationA.clone();
			fixed.setPitch(0);
			double x = RADIUS_MOVING * Math.cos(_theta), y = RADIUS_MOVING * Math.sin(_theta), r = Math.toRadians(fixed.getYaw());

			locationA.add(x, y, 0);
			locationB.subtract(x, y, 0);

			Vector vectorA = locationA.toVector().subtract(fixed.toVector()), vectorB = locationB.toVector().subtract(fixed.toVector());

			UtilAlg.rotateAroundYAxis(vectorA, r);
			UtilAlg.rotateAroundYAxis(vectorB, r);

			new ColoredParticle(ParticleType.RED_DUST, RED, fixed.clone().add(vectorA))
					.display();
			new ColoredParticle(ParticleType.RED_DUST, BLUE, fixed.clone().add(vectorB))
					.display();
		}
		else
		{
			locationA = player.getLocation().add(0, 0.1, 0);
			locationB = locationA.clone();
			double x = _radius * Math.cos(_theta), z = _radius * Math.sin(_theta);

			locationA.add(x, _y, z);
			locationB.add(-x, _y, -z);

			if (_up)
			{
				new ColoredParticle(ParticleType.RED_DUST, RED, locationA)
						.display();
				new ColoredParticle(ParticleType.RED_DUST, BLUE, locationB)
						.display();
			}
			else
			{
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, locationA, null, 0, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, locationB, null, 0, 1, ViewDist.NORMAL);
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

		if (_up)
		{
			_y += DELTA_Y;

			if (_y > MAX_HEIGHT)
			{
				_up = false;
			}
		}
		else
		{
			_y -= DELTA_Y;

			if (_y < 0)
			{
				_up = true;
			}
		}

		_theta += DELTA_THETA;
		_radius = (MAX_HEIGHT - _y) / MAX_HEIGHT * RADIUS;
		super.Caller(event);
	}
}
