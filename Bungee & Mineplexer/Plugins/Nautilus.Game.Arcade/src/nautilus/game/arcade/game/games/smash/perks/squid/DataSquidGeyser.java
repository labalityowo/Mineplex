package nautilus.game.arcade.game.games.smash.perks.squid;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class DataSquidGeyser
{
	public Player Player;
	public Set<Block> Blocks;
	public long StartTime;
	
	public DataSquidGeyser(Player player, Set<Block> blocks)
	{
		StartTime = System.currentTimeMillis();
		Player = player;
		Blocks = blocks;
	}
}
