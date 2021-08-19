package mineplex.core.common.block;

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
		restore(false);
	}
	
	public void restore(boolean requireNotAir)
	{
		if (requireNotAir && Block.getType() == org.bukkit.Material.AIR)
			return;
		
		Block.setTypeIdAndData(Material.getId(), Data, true);
	}
}
