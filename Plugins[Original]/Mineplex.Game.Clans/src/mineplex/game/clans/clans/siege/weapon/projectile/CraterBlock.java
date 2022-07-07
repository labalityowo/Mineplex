package mineplex.game.clans.clans.siege.weapon.projectile;

import org.bukkit.Location;
import org.bukkit.Material;

public class CraterBlock
{
	public Material Type;
	public byte Data;
	public double DistanceToOrigin;
	
	public Location Location;
	
	public CraterBlock(Location location, double dist, Material type, byte data)
	{
		Location = location;
		DistanceToOrigin = dist;
		Type = type;
		Data = data;
	}
	
	public CraterBlock(Location location, double dist, Material type)
	{
		this(location, dist, type, (byte) 0);
	}
	
	public void set()
	{
		Location.getBlock().setType(Type);
		Location.getBlock().setData(Data);
	}
}
