package mineplex.hub.parkour.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;

public class Snake
{

	private static final int TAIL_LENGTH = 10;

	private final List<Location> _path;
	private byte _color;

	private int _index = 0;
	private boolean _colorTick;

	public Snake(Location loc, List<Location> path)
	{
		_path = new ArrayList<>(path.size());

		//First
		_path.add(loc);
		MapUtil.QuickChangeBlockAt(loc, Material.BROWN_MUSHROOM);

		//Direction
		for (Block block : UtilBlock.getSurrounding(loc.getBlock(), false))
		{
			if (block.getType() == Material.WOOL)
			{
				_path.add(block.getLocation().add(0.5, 0.5, 0.5));
				_color = block.getData();
				MapUtil.QuickChangeBlockAt(block.getLocation(), Material.BROWN_MUSHROOM);
				break;
			}
		}

		//Path
		for (int i = 0; i < 100; i++)
		{
			Iterator<Location> pathIterator = path.iterator();

			while (pathIterator.hasNext())
			{
				Location pathLoc = pathIterator.next();

				if (UtilMath.offset(_path.get(_path.size() - 1).getBlock().getLocation(), pathLoc.getBlock().getLocation()) <= 1)
				{
					_path.add(pathLoc);
					MapUtil.QuickChangeBlockAt(pathLoc, Material.BROWN_MUSHROOM);
					pathIterator.remove();
				}
			}
		}
	}

	public void update()
	{
		if (_path.isEmpty())
		{
			return;
		}

		//Set Block
		MapUtil.QuickChangeBlockAt(_path.get(_index), Material.WOOL, getColor());

		int back = _index - TAIL_LENGTH;
		if (back < 0)
		{
			back += _path.size();
		}

		//Unset Tail
		MapUtil.QuickChangeBlockAt(_path.get(back), Material.BROWN_MUSHROOM);

		//ALT
		if (_path.size() > 50)
		{
			int newIndex = (_index + (_path.size() / 2)) % _path.size();

			//Set Block
			MapUtil.QuickChangeBlockAt(_path.get(newIndex), Material.WOOL, getColor());

			back = newIndex - TAIL_LENGTH;
			if (back < 0)
			{
				back += _path.size();
			}

			//Unset Tail
			MapUtil.QuickChangeBlockAt(_path.get(back), Material.BROWN_MUSHROOM);
		}

		_index = (_index + 1) % _path.size();
		_colorTick = !_colorTick;
	}

	public void clear()
	{
		for (Location location : _path)
		{
			MapUtil.QuickChangeBlockAt(location, Material.BROWN_MUSHROOM);
		}
	}

	private byte getColor()
	{
		return _colorTick ? _color : 0;
	}
}
