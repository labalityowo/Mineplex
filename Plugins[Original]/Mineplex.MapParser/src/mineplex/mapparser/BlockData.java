package mineplex.mapparser;

import org.bukkit.block.Block;
import org.bukkit.Material;

public class BlockData
{
	public Block Block;
	public Material Material;
	public byte Data;
	public long Time;
	
	public BlockData(Block block)
	{
		Block = block;
		Material = block.getType();
		Data = block.getData();
		Time = System.currentTimeMillis();
	}
	
	public void restore()
	{
		Block.setTypeIdAndData(Material.getId(), Data, true);
	}
}
