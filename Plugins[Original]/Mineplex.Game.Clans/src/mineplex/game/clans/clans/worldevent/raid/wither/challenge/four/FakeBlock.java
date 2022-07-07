package mineplex.game.clans.clans.worldevent.raid.wither.challenge.four;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.game.clans.clans.ClansManager;

public class FakeBlock
{
	private ChallengeFour _challenge;
	private Block _block;
	
	public FakeBlock(ChallengeFour challenge, Block block)
	{
		_challenge = challenge;
		_block = block;
		_block.setType(Material.NETHER_BRICK);
	}
	
	public void update()
	{
		boolean aired = false;
		for (Player player : _challenge.getRaid().getPlayers())
		{
			if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).equals(_block))
			{
				ClansManager.getInstance().getBlockRestore().restore(_block);
				_block.setType(Material.AIR);
				aired = true;
				break;
			}
		}
		
		if (!aired)
		{
			ClansManager.getInstance().getBlockRestore().restore(_block);
			_block.setType(Material.NETHER_BRICK);
		}
	}
}