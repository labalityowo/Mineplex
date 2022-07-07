package mineplex.core.gadget.gadgets.particle;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.particleeffects.BabyFireworkEffect;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleFiveYear extends ParticleGadget
{

	private final BufferedImage _bufferedImage;

	public ParticleFiveYear(GadgetManager manager)
	{
		super(manager, "Five Years of Mineplex", new String[]
				{
						C.cGray + "Celebrate Mineplex's 5th",
						C.cGray + "Anniversary with this aura!",
						C.blankLine,
						C.cBlue + "Earned by joining the server during",
						C.cBlue + "the Mineplex 5 Year Anniversary."
				}, CostConstants.NO_LORE, Material.ENDER_CHEST, (byte) 0);

		_bufferedImage = UtilText.stringToBufferedImage(new Font("Tahoma", Font.PLAIN, 12), "5");
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (Manager.isMoving(player))
		{
			new BabyFireworkEffect(player.getLocation().add(0, 0.3, 0), Color.ORANGE, Color.BLACK)
					.start();
		}
		else
		{
			Location location = player.getLocation().add(0, 1, 0);
			location.setPitch(0);
			Vector direction = location.getDirection();
			location.add(UtilAlg.getRight(direction).multiply(0.3));
			location.subtract(direction.multiply(0.5));

			int color, height = _bufferedImage.getHeight() / 2, width = _bufferedImage.getWidth() / 2;
			double yaw = Math.toRadians(location.getYaw());

			for (int y = 2; y < _bufferedImage.getHeight() - 1; y++)
			{
				for (int x = 0; x < _bufferedImage.getWidth() - 1; x++)
				{
					color = _bufferedImage.getRGB(x, y);

					Vector vector = new Vector(width - x, height - y, 0).multiply(0.2);
					UtilAlg.rotateAroundYAxis(vector, yaw);
					UtilParticle.PlayParticleToAll(Color.black.getRGB() == color ? ParticleType.FLAME : ParticleType.SMOKE, location.add(vector), null, 0, 1, ViewDist.NORMAL);
					location.subtract(vector);
				}
			}
		}
	}
}
