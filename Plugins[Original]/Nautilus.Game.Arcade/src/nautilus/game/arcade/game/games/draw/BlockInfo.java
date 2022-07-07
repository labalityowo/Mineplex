package nautilus.game.arcade.game.games.draw;

import org.bukkit.Material;

public class BlockInfo
{
	private Material _type;
	private byte _data;

	public BlockInfo(Material type, byte data)
	{
		_type = type;
		_data = data;
	}

	public Material getType()
	{
		return _type;
	}

	public byte getData()
	{
		return _data;
	}
}
