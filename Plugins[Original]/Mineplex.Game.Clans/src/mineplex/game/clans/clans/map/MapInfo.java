package mineplex.game.clans.clans.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mineplex.core.common.util.UtilTime;

public class MapInfo
{
	private int _scale;
	private int _centerX;
	private int _centerZ;
	private long _lastRendered;
	private boolean _sendMap;
	private List<Long> _lastZooms = new ArrayList<Long>();
	private int _mapId;

	public MapInfo(int newId)
	{
		_mapId = newId;
	}

	public int getMap()
	{
		return _mapId;
	}

	public void setMap(int newId)
	{
		_mapId = newId;
	}

	public boolean canZoom()
	{
		Iterator<Long> itel = _lastZooms.iterator();

		while (itel.hasNext())
		{
			long lastZoomed = itel.next();

			if (UtilTime.elapsed(lastZoomed, 2500))
			{
				itel.remove();
			}
		}

		return _lastZooms.size() < 3;
	}

	public void addZoom()
	{
		_lastZooms.add(System.currentTimeMillis());
	}

	public long getZoomCooldown()
	{
		long cooldown = 0;

		for (long zoomCooldown : _lastZooms)
		{
			if (cooldown == 0 || zoomCooldown < cooldown)
			{
				cooldown = zoomCooldown;
			}
		}

		return cooldown;
	}

	public long getLastRendered()
	{
		return _lastRendered;
	}

	public void setLastRendered()
	{
		_lastRendered = System.currentTimeMillis();
	}

	public void setInfo(int scale, int x, int z)
	{
		_lastRendered = 0;
		_scale = scale;
		_centerX = x;
		_centerZ = z;
		_sendMap = true;
	}

	public void setInfo(int x, int z)
	{
		_lastRendered = 0;
		_centerX = x;
		_centerZ = z;
		_sendMap = true;
	}

	public boolean isSendMap()
	{
		if (_sendMap)
		{
			_sendMap = false;
			return true;
		}

		return false;
	}

	public int getX()
	{
		return _centerX;
	}

	public int getZ()
	{
		return _centerZ;
	}

	public int getScale()
	{
		return _scale;
	}

}
