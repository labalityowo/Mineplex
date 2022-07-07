package mineplex.game.clans.clans.worldevent.raid.wither.challenge.three;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import mineplex.game.clans.clans.ClansManager;

public class ChallengeTorch
{
	private ChallengeThree _challenge;
	private Block _block;
	private long _extinguish;
	
	public ChallengeTorch(ChallengeThree challenge, Block block)
	{
		_challenge = challenge;
		_block = block;
		_extinguish = -1;
	}
	
	public void handleInteract(Block block)
	{
		if (block.equals(_block) && _extinguish == -1)
		{
			_challenge.LitTorches++;
			_extinguish = System.currentTimeMillis() + 3000;
			ClansManager.getInstance().getBlockRestore().restore(_block.getRelative(BlockFace.UP));
			_block.getRelative(BlockFace.UP).setType(Material.FIRE);
		}
	}
	
	public void update()
	{
		if (_extinguish != -1 && !_challenge.isComplete())
		{
			if (_extinguish < System.currentTimeMillis())
			{
				_extinguish = -1;
				_challenge.LitTorches--;
				ClansManager.getInstance().getBlockRestore().restore(_block.getRelative(BlockFace.UP));
				_block.getRelative(BlockFace.UP).setType(Material.AIR);
			}
		}
	}
}