package nautilus.game.arcade.game.games.monstermaze;

import org.bukkit.Material;

public class MazeBlockData
{
	public static class MazeBlock
	{
		public final Material Type;
		public final byte Data;
		
		public MazeBlock(Material type)
		{
			this(type, (byte) 0);
		}
		
		public MazeBlock(Material type, byte data)
		{
			Type = type;
			Data = data;
		}
	}
	
	public final MazeBlock Top;
	public final MazeBlock Middle;
	public final MazeBlock Bottom;
	
	public MazeBlockData(MazeBlock top, MazeBlock middle, MazeBlock bottom)
	{
		Top = top;
		Middle = middle;
		Bottom = bottom;
	}
}
