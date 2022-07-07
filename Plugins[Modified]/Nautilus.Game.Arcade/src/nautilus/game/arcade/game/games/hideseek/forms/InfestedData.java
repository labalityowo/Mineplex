package nautilus.game.arcade.game.games.hideseek.forms;

import org.bukkit.block.Block;

public class InfestedData
{
	public Block Block;
	public org.bukkit.Material Material;
	public byte Data;
	
	public InfestedData(Block block)
	{
		Block = block;
		Material = block.getType();
		Data = block.getData();
		
		block.setType(Material.AIR);
	}
	
	public void restore()
	{
		Block.setTypeIdAndData(Material.getId(), Data, true);
	}
}
