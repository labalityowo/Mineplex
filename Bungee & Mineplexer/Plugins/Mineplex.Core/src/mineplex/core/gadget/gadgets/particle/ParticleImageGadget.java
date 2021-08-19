package mineplex.core.gadget.gadgets.particle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleImageGadget extends ParticleGadget
{

	private static final short BACKGROUND = (short) -1;

	private final List<Pair<Vector, DustSpellColor>> _particles;
	double _yOffset = 1;

	ParticleImageGadget(GadgetManager manager, String name, String[] desc, String[] image, int cost, Material mat, byte data, String... altNames)
	{
		super(manager, name, desc, cost, mat, data, altNames);

		_particles = new ArrayList<>(200);

		int width = image.length / 2;
		int i = 0;
		for (String line : image)
		{
			String[] pixels = line.split("/");
			int height = pixels.length / 2;
			int j = 0;

			for (String pixel : pixels)
			{
				String[] rgb = pixel.split(",");
				short r = Short.valueOf(rgb[0]), g = Short.valueOf(rgb[1]), b = Short.valueOf(rgb[2]);

				if (r == BACKGROUND && g == BACKGROUND && b == BACKGROUND)
				{
					j++;
					continue;
				}

				Vector offset = new Vector(width - i, height - j++, 0).multiply(.125);
				DustSpellColor color = new DustSpellColor(r, g, b);
				_particles.add(Pair.create(offset, color));
			}

			i++;
		}
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Location location = player.getLocation().add(0, _yOffset, 0);
		location.setPitch(0);
		location.subtract(location.getDirection().multiply(0.5));
		double yaw = Math.toRadians(location.getYaw());

		_particles.forEach(pair ->
		{
			Vector offset = pair.getLeft().clone();
			UtilAlg.rotateAroundYAxis(offset, yaw);

			new ColoredParticle(ParticleType.RED_DUST, pair.getRight(), location.add(offset))
					.display();

			location.subtract(offset);
		});
	}
}
