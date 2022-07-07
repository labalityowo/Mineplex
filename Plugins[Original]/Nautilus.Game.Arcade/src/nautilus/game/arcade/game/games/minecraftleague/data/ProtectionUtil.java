package nautilus.game.arcade.game.games.minecraftleague.data;

import org.bukkit.block.Block;

public class ProtectionUtil {
	
	public static boolean isSameBlock(Block a, Block b)
	{
		if (a.getX() == b.getX())
			if (a.getY() == b.getY())
				return (a.getZ() == b.getZ());
		
		return false;
	}

}
