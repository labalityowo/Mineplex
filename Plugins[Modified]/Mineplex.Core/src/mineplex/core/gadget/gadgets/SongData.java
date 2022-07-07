package mineplex.core.gadget.gadgets;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class SongData
{
	public Block Block;
	public long EndTime;
	
	public SongData(Block block, long duration)
	{
		Block = block;
		EndTime = System.currentTimeMillis() + duration;
		
		Block.setType(Material.JUKEBOX);
	}
	
	public boolean update()
	{
		if (System.currentTimeMillis() > EndTime)
		{
			if (Block.getType() == Material.JUKEBOX)
				Block.setType(Material.AIR);
			
			return true;
		}
			
		UtilParticle.PlayParticle(ParticleType.NOTE, Block.getLocation().add(0.5, 1, 0.5), 0.5f, 0.5f, 0.5f, 0f, 2,
				ViewDist.NORMAL, UtilServer.getPlayers());

		return false;
	}
	
	/**
	 * Forces this song's jukebox to revert to air
	 */
	public void disable()
	{
		Block.setType(Material.AIR);
	}
}