package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.games.christmas.Christmas;

public class IceMaze 
{
	private Christmas Host;
	
	private ArrayList<Location> _corners;
	private ArrayList<Block> _blocks;
	private ArrayList<Location> _exits;
	
	private Location _present;
	
	private Comparator<Block> _heightComparator = new Comparator<Block>()
			{
				@Override
				public int compare(Block o1, Block o2)
				{
					if (o1.getLocation().getBlockY() == o2.getLocation().getBlockY())
						return 0;
					
					return o1.getLocation().getBlockY() > o2.getLocation().getBlockY() ? 1 : -1;
				}
			};
	
	public IceMaze(Christmas host, ArrayList<Location> mazeCorners, ArrayList<Location> mazeExits, Location[] presents)
	{
		Host = host;
		
		_corners = mazeCorners;
		_exits = mazeExits;
			
		//Set Present	
		if (UtilMath.offset(presents[0], _exits.get(0)) < UtilMath.offset(presents[1], _exits.get(0)))
		{
			_present = presents[0].getBlock().getLocation();
		}
		else
		{
			_present = presents[1].getBlock().getLocation();
		}
		
		for (Location loc : _corners)
			loc.getBlock().setType(Material.AIR);
		
		_blocks = UtilBlock.getInBoundingBox(_corners.get(0), _corners.get(1));
		
		
		
		//Exits
		Location exit = UtilAlg.Random(_exits);
		for (Location loc : _exits)
		{
			if (UtilMath.offset(loc, exit) < 3)
			{
				loc.getBlock().setType(Material.AIR);
			}
			else
			{
				loc.getBlock().setType(Material.ICE);
			}
		}
	}

	public void Update()
	{
		//No Presents
		if (!Host.GetSleigh().HasPresent(_present))
			return;
		
		//Finished
		if (_blocks.isEmpty())
			return;
		
		Collections.sort(_blocks, _heightComparator);
		
		for (int i=0 ; i<20 ; i++)
		{
			if (_blocks.isEmpty())
				break;
			
			Block block = _blocks.remove(_blocks.size() - 1);
			
			if (block.getType() == Material.ICE)
				block.setType(Material.AIR);
		}
	}
}
