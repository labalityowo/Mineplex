package nautilus.game.arcade.kit.perks.data;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;

public interface IBlockRestorer
{
	public void restoreBlock(Location loc, double radius);
	public void addBlocks(Set<Block> blocks);
}
