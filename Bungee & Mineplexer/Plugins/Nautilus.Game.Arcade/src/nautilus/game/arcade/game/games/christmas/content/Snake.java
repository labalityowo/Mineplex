package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Snake 
{
	private ArrayList<Location> _path = new ArrayList<Location>();
	private byte _color = 0;

	private int _index = 0;
	private boolean _colorTick = false;
	
	private int _pathId = 39;

	public Snake(Location loc, ArrayList<Location> path)
	{
		_path = new ArrayList<Location>();

		//First
		_path.add(loc);
		MapUtil.QuickChangeBlockAt(loc, _pathId, (byte)0);

		//Direction
		for (Block block : UtilBlock.getSurrounding(loc.getBlock(), false))
		{
			if (block.getType() == Material.WOOL)
			{
				_path.add(block.getLocation().add(0.5,0.5,0.5));
				_color = block.getData();
				MapUtil.QuickChangeBlockAt(block.getLocation(), _pathId, (byte)0);
				break;
			}
		}

		//Path
		for (int i=0 ; i<100 ; i++)
		{
			Iterator<Location> pathIterator = path.iterator();

			while (pathIterator.hasNext())
			{
				Location pathLoc = pathIterator.next();

				if (UtilMath.offset(_path.get(_path.size()-1).getBlock().getLocation(), pathLoc.getBlock().getLocation()) <= 1)
				{
					_path.add(pathLoc);
					MapUtil.QuickChangeBlockAt(pathLoc, _pathId, (byte)0);
					pathIterator.remove();
				}
			}
		}
	}

	public void Update()
	{
		if (_path.isEmpty())
			return;

		//Set Block
		MapUtil.QuickChangeBlockAt(_path.get(_index), 35, GetColor());

		int back = _index - 10;
		if (back < 0)
			back += _path.size();

		//Unset Tail
		MapUtil.QuickChangeBlockAt(_path.get(back), _pathId, (byte) 0);

		//ALT
		if (_path.size() > 50)
		{
			int newIndex = (_index + (_path.size()/2))%_path.size();
			
			//Set Block
			MapUtil.QuickChangeBlockAt(_path.get(newIndex), 35, GetColor());

			back = newIndex - 10;
			if (back < 0)
				back += _path.size();

			//Unset Tail
			MapUtil.QuickChangeBlockAt(_path.get(back), _pathId, (byte) 0);
		}
		
		_index = (_index+1)%_path.size();
		_colorTick = !_colorTick;
	}

	public byte GetColor()
	{
		if (_colorTick)	
			return _color;
		return 0;
	}
}
