package nautilus.game.arcade.kit.perks.data;

import org.bukkit.block.Block;
import org.bukkit.Material;

public class WoolBombData
{
	public Block Block;
	public long Time;
	public Material Material;
	public byte Data;
	
	public WoolBombData(Block block)
	{
		Block = block;
		Material = block.getType();
		Data = block.getData();
		
		Time = System.currentTimeMillis();
	}

	public void restore()
	{
		Block.setType(Material);
		Block.setData(Data);
	}
}
