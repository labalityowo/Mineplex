package mineplex.core.common.util.particles;

import java.awt.Color;

public class DustSpellColor extends ParticleColor
{

	private final int _red, _green, _blue;

	public DustSpellColor(org.bukkit.Color color)
	{
		_red = color.getRed();
		_green = color.getGreen();
		_blue = color.getBlue();
	}

	public DustSpellColor(Color color)
	{
		_red = color.getRed();
		_green = color.getGreen();
		_blue = color.getBlue();
	}

	public DustSpellColor(int r, int g, int b)
	{
		_red = r;
		_green = g;
		_blue = b;
	}

	public Color toAwtColor()
	{
		return new Color(_red, _green, _blue);
	}

	public float getX()
	{
		return (float) _red / 255f;
	}

	public float getY()
	{
		return (float) _green / 255f;
	}

	public float getZ()
	{
		return (float) _blue / 255f;
	}

}
