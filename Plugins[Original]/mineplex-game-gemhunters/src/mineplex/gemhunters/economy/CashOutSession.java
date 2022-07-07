package mineplex.gemhunters.economy;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class CashOutSession
{

	private double _current;
	private double _max;
	private ArmorStand _stand;
	private Location _location;
	
	public CashOutSession(Player player, double max)
	{
		_current = max;
		_max = max;
		_stand = player.getWorld().spawn(player.getLocation().add(0, 0.5, 0), ArmorStand.class);
		
		_stand.setCustomNameVisible(true);
		_stand.setVisible(false);
		_stand.setGravity(false);
		
		_location = player.getLocation();
	}
	
	public void endSession()
	{
		_stand.remove();
	}
	
	public void setCurrent(double current)
	{
		_current = current;
	}
	
	public double getCurrent()
	{
		return _current;
	}
	
	public double getMax()
	{
		return _max;
	}
	
	public ArmorStand getArmourStand()
	{
		return _stand;
	}
	
	public Location getLocation()
	{
		return _location;
	}
	
}
