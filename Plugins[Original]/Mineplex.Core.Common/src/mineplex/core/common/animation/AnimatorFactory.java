package mineplex.core.common.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * A small factory class to build animations using location inputs with embedded directions. It then calculates the vector difference
 * between the locations in an ordered fashion when building the list.
 */
public class AnimatorFactory
{
	
	private Map<Integer, Location> _locations = new HashMap<>();
	
	public void addLocation(Location loc, int tick)
	{
		_locations.put(tick, loc.clone());
	}
	
	public List<AnimationPoint> getBuildList(Location base)
	{
		List<AnimationPoint> list = new ArrayList<>();
		
		Iterator<Entry<Integer, Location>> it = _locations.entrySet().stream()
			.sorted((e1, e2) 
						-> Integer.compare(e1.getKey(), e2.getKey())
					)
			.iterator();
		
		while(it.hasNext())
		{
			Entry<Integer, Location> e = it.next();
			Vector diff = e.getValue().clone().subtract(base).toVector();
			
			list.add(new AnimationPoint(e.getKey(), diff, e.getValue().getDirection()));
		}
		
		return list;
	}
	
}
