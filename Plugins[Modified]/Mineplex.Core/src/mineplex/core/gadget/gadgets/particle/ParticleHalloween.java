package mineplex.core.gadget.gadgets.particle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleHalloween extends ParticleGadget
{

	private static final int ARRAY_SIZE = 300;
	private static final double INCREMENT_RANGE = 0.3;
	private static final double INCREMENT_THETA = Math.PI / 50;
	private static final double INCREMENT_THETA_2 = Math.PI / 3;
	private static final int INDEXES_PER_ITERATION = 15;
	private static final double MIN_RANGE = 0.2;
	private static final int MAX_RANGE = 3;

	private List<Double> _x, _z;
	private int[] _nextIndexes;
	private int _index, _inIndex;

	public ParticleHalloween(GadgetManager manager)
	{
		super(manager, "Halloween Aura", UtilText
						.splitLineToArray(C.cGray + "Scary Skeletons", LineFormat.LORE), CostConstants.FOUND_IN_TRICK_OR_TREAT,
				Material.PUMPKIN, (byte) 0);

		_nextIndexes = new int[INDEXES_PER_ITERATION];
		_x = new ArrayList<>(ARRAY_SIZE);
		_z = new ArrayList<>(ARRAY_SIZE);

		double theta = 0;
		int index = 0;

		for (double radius = MIN_RANGE; radius <= MAX_RANGE; radius += INCREMENT_RANGE)
		{
			for (double theta2 = 0; theta2 < 2 * Math.PI; theta2 += INCREMENT_THETA_2)
			{
				_x.add(radius * Math.sin(theta + theta2));
				_z.add(radius * Math.cos(theta + theta2));

				index++;
			}

			theta += INCREMENT_THETA;
		}

		_inIndex = index;

		for (double radius = MAX_RANGE; radius >= MIN_RANGE; radius -= INCREMENT_RANGE)
		{
			for (double theta2 = 0; theta2 < 2 * Math.PI; theta2 += INCREMENT_THETA_2)
			{
				_x.add(radius * Math.sin(theta + theta2));
				_z.add(radius * Math.cos(theta + theta2));

				index++;
			}

			theta += INCREMENT_THETA;
		}
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location location = player.getLocation().add(0, 0.3, 0);
		DustSpellColor color = new DustSpellColor(_index < _inIndex ? Color.BLACK : Color.ORANGE);

		if (Manager.isMoving(player))
		{
			for (int i = 0; i < 3; i++)
			{
				new ColoredParticle(ParticleType.RED_DUST, color, UtilAlg.getRandomLocation(location, 0.5, 0.1, 0.5))
						.display();
			}
		}
		else
		{
			for (int index : _nextIndexes)
			{
				double x = _x.get(index);
				double z = _z.get(index);

				location.add(x, 0, z);

				new ColoredParticle(ParticleType.RED_DUST, color, location)
						.display();

				location.subtract(x, 0, z);
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

		for (int i = 0; i < _nextIndexes.length; i++)
		{
			if (++_index == _x.size())
			{
				_index = 0;
			}

			_nextIndexes[i] = _index;
		}

		super.Caller(event);
	}
}
