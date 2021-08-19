package mineplex.core.gadget.gadgets.kitselector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.RGBData;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.KitSelectorGadget;

public class HaloKitSelector extends KitSelectorGadget
{

	private static final int PARTICLES_PER_CIRCLE = 20;
	private static final double RADIUS = 0.6;

	private List<Color> _colors;
	private int _steps = 0;

	public HaloKitSelector(GadgetManager manager)
	{
		super(manager, "Halo", UtilText.splitLinesToArray(new String[]{C.cGray + "Fight like an Angel."}, LineFormat.LORE),
				0, Material.GOLD_HELMET, (byte) 0);
		_colors = new ArrayList<>();
	}

	@Override
	public void playParticle(Entity entity, Player playTo)
	{
		if (_colors.isEmpty())
		{
			RGBData rgbData = UtilColor.hexToRgb(0xffd700);
			_colors.add(new Color(rgbData.getFullRed(), rgbData.getFullGreen(), rgbData.getFullBlue()));
			rgbData = UtilColor.hexToRgb(0xdaa520);
			_colors.add(new Color(rgbData.getFullRed(), rgbData.getFullGreen(), rgbData.getFullBlue()));
		}

		Location location = entity.getLocation().add(0, getEntityYOffset(entity), 0);
		double increment = (2 * Math.PI) / PARTICLES_PER_CIRCLE;
		double angle = _steps * increment;
		Vector vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
		ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(getNextColor()), location.add(vector));
		coloredParticle.display(UtilParticle.ViewDist.NORMAL, playTo);
		_steps++;
	}

	private Color getNextColor()
	{
		int r = UtilMath.random.nextInt(_colors.size());
		return _colors.get(r);
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
				return 1.3;
			case SPIDER:
			case CAVE_SPIDER:
				return 0.75;
			case ENDERMAN:
				return 3.3;
		}
		return 2.3;
	}

}
