package mineplex.core.common.util;

public enum LineFormat
{
	LORE(220),
	PUNISHMENT_UI(48),
	CHAT(319);

	private int _length;

	private LineFormat(int length)
	{
		_length = length;
	}

	public int getLength()
	{
		return _length;
	}
}