package nautilus.game.arcade.game.games.castleassault.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;

import nautilus.game.arcade.game.GameTeam;

public class TeamCrystal 
{
	private Location _loc;
	private GameTeam _owner;
	private EnderCrystal _crystal;
	private boolean _destroyed;
	
	public TeamCrystal(GameTeam owner, Location loc)
	{
		_owner = owner;
		_loc = loc;
		
		spawn();
	}
	
	public GameTeam getOwner()
	{
		return _owner;
	}
	
	public Location getLocation()
	{
		return _loc;
	}
	
	public boolean isActive()
	{
		return !_destroyed;
	}
	
	public void spawn()
	{
		_destroyed = false;
		_crystal = _loc.getWorld().spawn(_loc, EnderCrystal.class);
		_loc.getBlock().getRelative(0, -2, 0).setType(Material.BEACON);
	}
	
	public void destroy()
	{
		_destroyed = true;
		_crystal.remove();
		_crystal = null;
		_loc.getBlock().getRelative(0, -2, 0).setType(Material.SMOOTH_BRICK);
	}
}