package mineplex.core.progression.data;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.UUID;

/**
 * Player Wrapper for the Progressive kit system
 */
public class PlayerKit
{

	private UUID _player;
	private String _currentKit;
	private Map<String, Integer> _kitLevels;
	private Map<String, Integer> _kitXPs;
	private Map<String, Integer> _kitUpgradeLevels;
	private Map<String, Boolean> _kitDefaultTypes;

	//We only want players to level up kits once per game
	private boolean _hasLeveledUp;

	public PlayerKit(UUID player)
	{
		_player = player;
		_kitLevels = Maps.newHashMap();
		_kitXPs = Maps.newHashMap();
		_kitUpgradeLevels = Maps.newHashMap();
		_kitDefaultTypes = Maps.newHashMap();
	}

	public String getCurrentKit()
	{
		return _currentKit;
	}

	public void setCurrentKit(String currentKit)
	{
		_currentKit = currentKit;
	}

	public int getLevel(String kit)
	{
		return _kitLevels.getOrDefault(kit, 1);
	}

	public int getXp(String kit)
	{
		return _kitXPs.getOrDefault(kit, 1);
	}

	public int getUpgradeLevel(String kit)
	{
		return _kitUpgradeLevels.getOrDefault(kit, 0);
	}

	public void setLevel(int level, String kit)
	{
		_kitLevels.put(kit, level);
	}

	public void setXp(int xp, String kit)
	{
		_kitXPs.put(kit, xp);
	}

	public void setUpgradeLevel(int upgradeLevel, String kit)
	{
		_kitUpgradeLevels.put(kit, upgradeLevel);
	}

	public boolean isDefault(String kit)
	{
		return _kitDefaultTypes.getOrDefault(kit, true);
	}

	public void setDefault(boolean value, String kit)
	{
		_kitDefaultTypes.put(kit, value);
	}

	public boolean hasLeveledUp()
	{
		return _hasLeveledUp;
	}

	public void levelUp()
	{
		this._hasLeveledUp = true;
	}

	public UUID getPlayer()
	{
		return _player;
	}

	public UUID getUuid()
	{
		return _player;
	}

}
