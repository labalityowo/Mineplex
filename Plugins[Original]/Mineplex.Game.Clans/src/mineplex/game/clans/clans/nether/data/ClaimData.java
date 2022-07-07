package mineplex.game.clans.clans.nether.data;

import org.bukkit.block.Block;

/**
 * Data class to hold specified player portal corners
 */
public class ClaimData
{
	private Block _first, _second;
	
	/**
	 * Fetches the first selected corner
	 * @return The first selected corner
	 */
	public Block getFirstCorner()
	{
		return _first;
	}
	
	/**
	 * Fetches the second selected corner
	 * @return The second selected corner
	 */
	public Block getSecondCorner()
	{
		return _second;
	}
	
	/**
	 * Fetches the total count of selected corners
	 * @return The number of selected corners
	 */
	public int getTotalSelected()
	{
		int total = 2;
		if (_first == null)
		{
			total--;
		}
		if (_second == null)
		{
			total--;
		}
		
		return total;
	}
	
	/**
	 * Sets the first selected corner
	 * @param block The block to set the corner to
	 */
	public void setFirstCorner(Block block)
	{
		_first = block;
	}
	
	/**
	 * Sets the first selected corner
	 * @param block The block to set the corner to
	 */
	public void setSecondCorner(Block block)
	{
		_second = block;
	}
}