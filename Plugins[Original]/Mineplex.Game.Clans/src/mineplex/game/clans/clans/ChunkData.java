package mineplex.game.clans.clans;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public class ChunkData 
{
	
	private int _x;
	public int getX() { return _x; }
	
	private int _z;
	public int getZ() { return _z; }
	
	private ChatColor _color;
	public ChatColor getColor() { return _color; }
	
	private String _clanName;
	public String getClanName() { return _clanName; }
	
	public ChunkData(int x, int z, ChatColor color, String clanName)
	{
		_x = x;
		_z = z;
		_color = color;
		_clanName = clanName;
	}
}