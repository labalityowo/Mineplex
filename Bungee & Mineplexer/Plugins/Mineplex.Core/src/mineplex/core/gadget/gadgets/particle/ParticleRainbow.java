package mineplex.core.gadget.gadgets.particle;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetRainbow;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleRainbow extends ParticleGadget
{

	private static final double STARTING_RADIUS = 0.7;
	private static final double SEPARATING_RADIUS = 0.1;
	private static final double MAX_THETA = 2 * Math.PI;
	private static final double VECTOR_THETA = Math.PI / 200;

	private final AchievementManager _achievementManager;
	private final Map<Player, Integer> _rings;

	private double _vectorTheta;

	public ParticleRainbow(GadgetManager manager, AchievementManager achievementManager)
	{
		super(manager, "Rainbow Aura", SetRainbow.GADGET_LORE, CostConstants.NO_LORE, Material.WOOL, (byte) (UtilMath.r(14) + 1));

		_achievementManager = achievementManager;
		_rings = new HashMap<>();
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		super.enableCustom(player, message);

		int size = Math.min(SetRainbow.PER_LEVEL, 1 + _achievementManager.getMineplexLevelNumber(player) / SetRainbow.PER_LEVEL);

		_rings.put(player, size);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		_rings.remove(player);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location location = player.getLocation().add(0, 1, 0);
		int rings = _rings.get(player);

		if (Manager.isMoving(player))
		{
			for (int i = 0; i < rings; i++)
			{
				DustSpellColor colour = SetRainbow.COLOURS[i];
				double x = Math.random() - 0.5;
				double y = Math.random() - 0.5;
				double z = Math.random() - 0.5;

				location.add(x, y, z);

				new ColoredParticle(ParticleType.RED_DUST, colour, location)
						.display();

				location.subtract(x, y, z);
			}
		}
		else
		{
			for (int i = 0; i < rings; i++)
			{
				DustSpellColor colour = SetRainbow.COLOURS[i];
				int j = i + 1;
				double vectorTheta = _vectorTheta + (MAX_THETA / j);
				double r = STARTING_RADIUS + SEPARATING_RADIUS * j;
				double d = MAX_THETA / (7 * r);

				for (double theta = 0; theta < MAX_THETA; theta += d)
				{
					double x = r * Math.cos(theta);
					double z = r * Math.sin(theta);
					Vector vector = new Vector(x, 0, z);
					vector = UtilAlg.rotateAroundXAxis(vector, vectorTheta);
					vector = UtilAlg.rotateAroundYAxis(vector, vectorTheta);

					location.add(vector);

					new ColoredParticle(ParticleType.RED_DUST, colour, location)
							.display();

					location.subtract(vector);
				}
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

		super.Caller(event);
		_vectorTheta += VECTOR_THETA;
	}
}
