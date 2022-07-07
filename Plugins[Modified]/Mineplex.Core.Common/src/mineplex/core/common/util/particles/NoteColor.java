package mineplex.core.common.util.particles;

import org.bukkit.Color;

public class NoteColor extends ParticleColor
{

	private int _red, _green, _blue;

	public NoteColor(Color color)
	{
		_red = color.getRed();
		_green = color.getGreen();
		_blue = color.getBlue();
	}

	public NoteColor(int r, int g, int b)
	{
		_red = r;
		_green = g;
		_blue = b;
	}

	public float getX()
	{
		return (float) _red / 24f;
	}

	public float getY()
	{
		return 0f;
	}

	public float getZ()
	{
		return 0f;
	}

}
