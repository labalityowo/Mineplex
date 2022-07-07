package mineplex.core.particleeffects;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;

public class RainbowTauntEffect extends Effect
{

	private static final int PARTICLES = 25;
	private static final double HEIGHT = 4;

	private static final Color RED = new Color(255, 0, 0);
	private static final Color ORANGE = new Color(255, 127, 0);
	private static final Color YELLOW = new Color(255, 255, 0);
	private static final Color GREEN = new Color(0, 255, 0);
	private static final Color BLUE = new Color(0, 0, 255);
	private static final Color INDIGO = new Color(75, 0, 130);
	private static final Color VIOLET = new Color(143, 0, 255);

	public RainbowTauntEffect(Location start)
	{
		super(16, new EffectLocation(start), 5);
	}

	@Override
	public void runEffect()
	{
		Location location = getEffectLocation().getFixedLocation();
		Location target = getTargetLocation().getFixedLocation();
		Color[] colors = new Color[]{VIOLET, INDIGO, BLUE, GREEN, YELLOW, ORANGE, RED};

		for (int i = 0; i < 7; i++)
		{
			line(location.clone().add(0, 0.25 * i, 0), target.clone().add(0, 0.25 * i, 0), colors[i]);
		}
	}

	private void line(Location startLocation, Location targetLocation, Color color)
	{
		Vector link = targetLocation.toVector().subtract(startLocation.toVector());
		float length = (float) link.length();
		float pitch = (float) (4 * HEIGHT / Math.pow(length, 2));
		for (int i = 0; i < PARTICLES; i++)
		{
			Vector v = link.clone().normalize().multiply(length * i / PARTICLES);
			float x = ((float) i / PARTICLES) * length - length / 2;
			float y = (float) (-pitch * Math.pow(x, 2) + HEIGHT);
			startLocation.add(v).add(0, y, 0);
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(color), startLocation);
			coloredParticle.display();
			startLocation.subtract(0, y, 0).subtract(v);
		}
	}

}
