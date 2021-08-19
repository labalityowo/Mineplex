package mineplex.staffServer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class LinkedTemporaryItem<K, V>
{
	private Map<K, V> _primaryMap;
	private Map<Player, K> _playerMap;

	public LinkedTemporaryItem()
	{
		_primaryMap = new HashMap<>();
		_playerMap = new HashMap<>();
	}

	public boolean remove(Player player)
	{
		if (_playerMap.containsKey(player))
		{
			_primaryMap.remove(_playerMap.remove(player));
			return true;
		}

		return false;
	}

	public Map<K, V> getPrimaryMap()
	{
		return _primaryMap;
	}

	public void put(Player player, K key, V value)
	{
		_playerMap.put(player, key);
		_primaryMap.put(key, value);
	}

	public V get(K key)
	{
		return getPrimaryMap().get(key);
	}
}
