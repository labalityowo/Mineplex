package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleDeepSeaSwirl extends ParticleGadget
{

	private static final DustSpellColor[] COLOURS =
			{
					new DustSpellColor(Color.AQUA),
					new DustSpellColor(Color.TEAL),
					new DustSpellColor(Color.BLUE)
			};

	private static final double MAX_RADIUS = 2.2;
	private static final int SPIRALS = 3;
	private static final double DELTA_THETA = Math.PI / 60;
	private static final double DELTA_THETA_2 = 2 * Math.PI / SPIRALS;

	private double _theta;
	private int _index;

	public ParticleDeepSeaSwirl(GadgetManager manager)
	{
		super(manager, "Deep Sea Swirl", UtilText.splitLineToArray(C.cGray + "Under the sea!!", LineFormat.LORE), CostConstants.FOUND_IN_TREASURE_CHESTS, Material.POTION, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location location = player.getLocation().add(0, 0.25, 0);
		DustSpellColor colour = COLOURS[_index];

		if (Manager.isMoving(player))
		{
			new ColoredParticle(ParticleType.RED_DUST, colour, location)
					.display();
		}
		else
		{
			for (double radius = 0.2; radius < MAX_RADIUS; radius += 0.1)
			{
				double t = radius / MAX_RADIUS * DELTA_THETA_2;

				for (int i = 0; i < SPIRALS; i++)
				{
					double x = radius * Math.sin(_theta + t), z = radius * Math.cos(_theta + t);

					location.add(x, 0, z);

					new ColoredParticle(ParticleType.RED_DUST, colour, location)
							.display();

					location.subtract(x, 0, z);
					t += DELTA_THETA_2;
				}
			}
		}
	}

	@Override
	@EventHandler
	public void Caller(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTEST)
		{
			_theta += DELTA_THETA;
			super.Caller(event);
		}
		else if (event.getType() == UpdateType.SEC)
		{
			_index = (_index + 1) % COLOURS.length;
		}
	}
}
