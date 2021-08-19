package mineplex.core.game.kit;

import java.util.HashMap;
import java.util.Map;

import mineplex.core.game.kit.upgrade.KitStat;

public class PlayerKitData
{

	private boolean _active;
	private final Map<KitStat, Integer> _stats;

	public PlayerKitData(boolean active)
	{
		_active = active;
		_stats = new HashMap<>();
	}

	public void setActive(boolean active)
	{
		_active = active;
	}

	public boolean isActive()
	{
		return _active;
	}

	public Map<KitStat, Integer> getStats()
	{
		return _stats;
	}
}
