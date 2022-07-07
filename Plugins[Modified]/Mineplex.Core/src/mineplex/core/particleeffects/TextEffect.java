package mineplex.core.particleeffects;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;

public class TextEffect extends Effect
{

	private static final double IMAGE_SIZE = 0.2;
	private static final Font FONT = new Font("Tahoma", Font.PLAIN, 16);

	private final boolean _realTime;
	private final boolean _invert;
	private final ParticleType _particleType;

	private String _text;
	private BufferedImage _bufferedImage;

	public TextEffect(int ticks, String text, Location location, boolean realTime, boolean invert, ParticleType particleType)
	{
		super(ticks, new EffectLocation(location));

		_text = text;
		_realTime = realTime;
		_invert = invert;
		_particleType = particleType;
		_period = 4;
	}

	public void setText(String text)
	{
		if (!_realTime)
		{
			return;
		}

		_text = text;
	}

	@Override
	public void runEffect()
	{
		if (_text == null)
		{
			stop();
			return;
		}

		if (_bufferedImage == null || _realTime)
		{
			_bufferedImage = UtilText.stringToBufferedImage(FONT, _text);
		}

		Location location = _effectLocation.getFixedLocation();
		int color, height = _bufferedImage.getHeight() / 2, width = _bufferedImage.getWidth() / 2;
		double yaw = Math.toRadians(location.getYaw() + 180);

		for (int y = 0; y < _bufferedImage.getHeight(); y++)
		{
			for (int x = 0; x < _bufferedImage.getWidth(); x++)
			{
				color = _bufferedImage.getRGB(x, y);

				if (!_invert && java.awt.Color.black.getRGB() != color)
				{
					continue;
				}
				else if (_invert && java.awt.Color.black.getRGB() == color)
				{
					continue;
				}

				Vector vector = new Vector(width - x, height - y, 0).multiply(IMAGE_SIZE);
				UtilAlg.rotateAroundYAxis(vector, yaw);
				UtilParticle.PlayParticleToAll(_particleType, location.add(vector), 0, 0, 0, 0, 1, ViewDist.LONG);
				location.subtract(vector);
			}
		}
	}
}
