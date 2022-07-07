package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetLegend;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.inventory.ClientItem;
import mineplex.core.inventory.data.Item;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleLegend extends ParticleGadget
{
	private static final double PI = Math.PI;
	private static final int BASE_PILLARS = 9;
	private static final int PILLAR_VARIANCE = 8;
	private static final int MOVING_PARTICLES = 8;
	private static final double VERTICAL_SPEED = 0.1;
	private static final double HEIGHT_VARIANCE = 0.8;
	private static final double ROTATIONAL_SPEED = .03;
	private static final double RADIAL_VARIANCE = 0.09;
	private static final double BASE_RADIUS = 1.30;
	private static final double HEIGHT_MODIFIER_BASE = 0.1;
	private static final double HEIGHT_MODIFIER_MAX = 1.3;
	private static final double HEIGHT_MODIFIER_INTERVAL = 0.15;

	private final int _pillars = pillars();
	private final DustSpellColor[] _colors = colors();
	private final double[] _heights = heights();
	private final double[] _verticals = verticals();
	private final double[] _variance = variances();
	private final double[] _thetas = thetas();
	private final double[] _radii = radii();

	public ParticleLegend(GadgetManager manager)
	{
		super(manager, "Legendary Aura",
				UtilText.splitLineToArray(C.cGray + "Legendary energy protects you.", LineFormat.LORE),
				CostConstants.UNLOCKED_WITH_LEGEND,
				Material.ENDER_PORTAL_FRAME, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Location location = player.getLocation();

		if (Manager.isMoving(player))
		{
			for (int i = 0; i < MOVING_PARTICLES; i++)
			{
				new ColoredParticle(ParticleType.RED_DUST, _colors[i % _colors.length], UtilMath.gauss(location, 8, 4, 8))
						.display();
			}
		}
		else if (event.getTick() % (ROTATIONAL_SPEED * 100) == 0)
		{
			boolean setBonus = getSet().isActive(player) && Math.random() < 0.05;

			for (int i = 0; i < _pillars; i++)
			{
				_thetas[i] = rollover(_thetas[i], ROTATIONAL_SPEED);
				_heights[i] = rollover(_heights[i], _verticals[i]);

				double x = (_radii[i] * Math.cos(_thetas[i])) + location.getX();
				double z = (_radii[i] * Math.sin(_thetas[i])) + location.getZ();
				double y = (Math.sin(_heights[i]) * _variance[i]) + location.getY();

				for (double h = HEIGHT_MODIFIER_BASE; h <= HEIGHT_MODIFIER_MAX; h += HEIGHT_MODIFIER_INTERVAL)
				{
					new ColoredParticle(ParticleType.RED_DUST, _colors[i % _colors.length], new Location(location.getWorld(), x, y + h, z))
							.display();
				}

				if (setBonus)
				{
					UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, new Location(location.getWorld(), x, y + HEIGHT_MODIFIER_MAX, z), null, 0, 1, ViewDist.NORMAL);
				}
			}
		}
	}

	private double[] heights()
	{
		double[] array = new double[_pillars];

		for (int i = 0; i < _pillars; i++)
		{
			array[i] = 6.28 * Math.random();
		}

		return array;
	}

	private double[] variances()
	{
		double[] array = new double[_pillars];

		for (int i = 0; i < _pillars; i++)
		{
			array[i] = Math.random() * HEIGHT_VARIANCE;
		}

		return array;
	}

	private double[] verticals()
	{
		double[] array = new double[_pillars];

		for (int i = 0; i < _pillars; i++)
		{
			array[i] = Math.random() * VERTICAL_SPEED;
		}

		return array;
	}

	private double[] thetas()
	{
		double[] array = new double[_pillars];
		double theta = 0;
		double interval = (2 * PI) / _pillars;

		for (int i = 0; i < _pillars; i++)
		{
			array[i] = theta;
			theta += interval;
		}

		return array;
	}

	private double[] radii()
	{
		double[] array = new double[_pillars];

		for (int i = 0; i < _pillars; i++)
		{
			array[i] = BASE_RADIUS + (Math.random() * RADIAL_VARIANCE);
		}

		return array;
	}

	private DustSpellColor[] colors()
	{
		DustSpellColor[] array = new DustSpellColor[_pillars];

		for (int i = 0; i < _pillars; i++)
		{
			array[i] = SetLegend.SELECTABLE_COLORS[i % SetLegend.SELECTABLE_COLORS.length];
		}

		return array;
	}

	private int pillars()
	{
		return BASE_PILLARS + (int) ((Math.random() * PILLAR_VARIANCE) - (PILLAR_VARIANCE / 2));
	}

	private double rollover(double value, double additive)
	{
		value += additive;

		if (value >= 2 * PI)
		{
			value = value - (2 * PI);
		}

		return value;
	}

	@EventHandler
	public void legendOwner(PlayerJoinEvent event)
	{
		// TODO HARDCODED Legendary Aura Database Item Id - 552
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.LEGEND_PARTICLE_EFFECT))
		{
			Manager.getInventoryManager().Get(event.getPlayer()).addItem(new ClientItem(new Item(552, getName()), 1));
		}
	}
}