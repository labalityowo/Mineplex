package mineplex.mavericks.review;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.mavericks.MavericksBuildWrapper;
/**
 * A simple wrapper class used to hold build data related to a location and player.
 */
public class ReviewData
{
	private Player _player;
	private MavericksBuildWrapper _data;
	private List<MavericksBuildWrapper> _logg = new ArrayList<>();
	private Location _loc;
	
	public ReviewData(Player player, Location loc)
	{
		_player = player;
		_loc = loc.getBlock().getLocation().add(0.5, 0, 0.5);
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Location getLoc()
	{
		return _loc.clone();
	}
	
	public MavericksBuildWrapper getData()
	{
		return _data;
	}
	
	public void setData(MavericksBuildWrapper data)
	{
		if (!_logg.contains(_data))
		{
			_logg.add(_data);
		}
		if (!_logg.contains(data))
		{
			_logg.add(data);
		}
		_data = data;
	}
	
	public MavericksBuildWrapper getPrevious()
	{
		int index = _logg.indexOf(_data);
		
		if (index <= 0)
		{
			return null;
		}
		
		MavericksBuildWrapper data = _logg.get(index-1);
		if (data != null && data.isReviewed())
		{
			_data = data;
			return getPrevious();
		}
		return data;
	}
	
	public MavericksBuildWrapper getNext()
	{
		int index = _logg.indexOf(_data);
		
		if (index == -1)
		{
			return null;
		}
		if (index+1 >= _logg.size())
		{
			return null;
		}
		
		MavericksBuildWrapper data = _logg.get(index+1);
		if (data != null && data.isReviewed())
		{
			_data = data;
			return getNext();
		}
		return data;
	}
	
	public boolean containsData(MavericksBuildWrapper data)
	{
		return _logg.contains(data);
	}
	
	public Location getAreaMin()
	{
		return _loc.getBlock().getLocation().add(MavericksReviewManager.OFFSET_VECTOR).subtract(1, 1, 12);
	}
	
	public Location getAreaMax()
	{
		return getAreaMin().add(24, 28, 24);
	}
	
	public List<Entity> getEntitiesInArea()
	{
		List<Entity> list = new ArrayList<>();
		for (Entity e : _loc.getWorld().getEntities())
		{
			if (e instanceof Player)
			{
				continue;
			}
			if (isInsideArea(e.getLocation()))
			{
				list.add(e);
			}
		}
		return list;
	}
	
	public boolean isInsideArea(Location loc)
	{
		return loc.toVector().isInAABB(getAreaMin().toVector(), getAreaMax().toVector());
	}
}