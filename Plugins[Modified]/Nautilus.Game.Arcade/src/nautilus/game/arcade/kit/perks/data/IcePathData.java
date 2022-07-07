package nautilus.game.arcade.kit.perks.data;

import java.util.ArrayList;

import mineplex.core.common.util.UtilMath;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class IcePathData 
{
	private ArrayList<Block> _blocks; 
	
	public IcePathData(Player player)
	{
		_blocks = new ArrayList<Block>(); 
		
		//Add Blocks
		if (Math.abs(player.getLocation().getDirection().getX()) > Math.abs(player.getLocation().getDirection().getZ()))
		{
			GetBlocks(player.getLocation().add(0, 0, 1), 16);
			GetBlocks(player.getLocation().add(0, 0, -1), 16);
		}
		else
		{
			GetBlocks(player.getLocation().add(1, 0, 0), 16);
			GetBlocks(player.getLocation().add(-1, 0, 0), 16);
		}
		
		GetBlocks(player.getLocation(), 16);
		
		//Sort Blocks
		for (int i=0 ; i<_blocks.size() ; i++)
		{
			for (int j=0 ; j+1<_blocks.size() ; j++)
			{
				if (UtilMath.offset(player.getLocation(), _blocks.get(j).getLocation().add(0.5, 0.5, 0.5)) > 
					UtilMath.offset(player.getLocation(), _blocks.get(j+1).getLocation().add(0.5, 0.5, 0.5)))
				{
					Block temp = _blocks.get(j);
					_blocks.set(j, _blocks.get(j+1));
					_blocks.set(j+1, temp);
				}
			}	
		}
	}
	
	public void GetBlocks(Location loc, int length)
	{
		//Below Player
		loc.subtract(0, 1, 0);
		
		Vector dir = loc.getDirection();
		
		double hLength = Math.sqrt(dir.getX()*dir.getX() + dir.getZ()*dir.getZ());
		
		if (Math.abs(dir.getY()) > hLength)
		{
			if (dir.getY() > 0)
				dir.setY(hLength);
			else
				dir.setY(-hLength);
			
			dir.normalize();
		}
		
		//Backtrack
		loc.subtract(dir.clone().multiply(2));
		
		double dist = 0;
		while (dist < length)
		{
			dist += 0.2;
			
			loc.add(dir.clone().multiply(0.2));
			
			if (loc.getBlock().getType() == Material.ICE)
				continue;
			
			if (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Material.SNOW)
			{
				if (!_blocks.contains(loc.getBlock()))
				{
					_blocks.add(loc.getBlock());
				}
			}
		}
	}
	
	public Block GetNextBlock()
	{
		if (_blocks.isEmpty())
			return null;
		
		return _blocks.remove(0);
	}
}
