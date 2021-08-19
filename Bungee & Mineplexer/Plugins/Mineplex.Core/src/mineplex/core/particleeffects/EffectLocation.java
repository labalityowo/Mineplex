package mineplex.core.particleeffects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EffectLocation
{

	private Location _location;
	private Entity _entity;

	public EffectLocation(Location location)
	{
		_location = location;
		_entity = null;
	}

	public EffectLocation(Entity entity)
	{
		_location = entity.getLocation();
		_entity = entity;
	}

	public Location getLocation()
	{
		if (_entity != null && _entity.isValid() && !_entity.isDead())
			return _entity.getLocation().clone();
		if (_location != null)
			return _location.clone();
		return null;
	}

	public Location getFixedLocation()
	{
		if (_location != null)
			return _location.clone();
		return null;
	}

}
