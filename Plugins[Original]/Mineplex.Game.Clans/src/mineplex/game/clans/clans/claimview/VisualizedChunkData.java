package mineplex.game.clans.clans.claimview;

import java.util.List;

import org.bukkit.Chunk;

import net.minecraft.server.v1_8_R3.EnumDirection;

public class VisualizedChunkData
{
	private List<EnumDirection> _displayableEdges;
	private Chunk _chunk;
	
	public long _start;
	
	public VisualizedChunkData(Chunk chunk, List<EnumDirection> dir)
	{
		_chunk = chunk;
		_displayableEdges = dir;
		_start = System.currentTimeMillis();
	}
	
	public double getLife()
	{
		return (double) (System.currentTimeMillis() - _start);
	}
	
	public boolean shouldDisplayEdge(int x, int z)
	{
		if (z == 15 && !_displayableEdges.contains(EnumDirection.SOUTH))
		{
			return false;
		}
		
		if (x == 15 && !_displayableEdges.contains(EnumDirection.EAST))
		{
			return false;
		}
		
		if (x == 0 && !_displayableEdges.contains(EnumDirection.WEST))
		{
			return false;
		}
		
		if (z == 0 && !_displayableEdges.contains(EnumDirection.NORTH))
		{
			return false;
		}
		
		return true;
	}

	public Chunk getChunk()
	{
		return _chunk;
	}
}
