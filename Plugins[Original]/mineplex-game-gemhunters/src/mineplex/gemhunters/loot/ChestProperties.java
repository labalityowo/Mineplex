package mineplex.gemhunters.loot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class ChestProperties
{

	private final String _name;
	private final Material _blockMaterial;
	private final String _dataKey;
	private final int _minAmount;
	private final int _maxAmount;
	private final int _maxChestPerLoc;
	private final int _spawnRate;
	private final int _expireRate;
	private final int _spawnRadius;
	private final int _maxActive;

	private final Map<Integer, Integer> _spawnedIndexes;
	private long _lastSpawn;

	public ChestProperties(String name, Material blockMaterial, String dataKey, int minAmount, int maxAmount, int maxChestPerLoc, int spawnRate, int expireRate, int spawnRadius, int maxActive)
	{
		_name = name;
		_blockMaterial = blockMaterial;
		_dataKey = dataKey;
		_minAmount = minAmount;
		_maxAmount = maxAmount;
		_maxChestPerLoc = maxChestPerLoc;
		_spawnRate = spawnRate;
		_expireRate = expireRate;
		_spawnRadius = spawnRadius;
		_maxActive = maxActive;

		_spawnedIndexes = new HashMap<>();
		setLastSpawn();
	}

	public final String getName()
	{
		return _name;
	}

	public final Material getBlockMaterial()
	{
		return _blockMaterial;
	}

	public final String getDataKey()
	{
		return _dataKey;
	}

	public final int getMinAmount()
	{
		return _minAmount;
	}

	public final int getMaxAmount()
	{
		return _maxAmount;
	}

	public final int getMaxChestPerLocation()
	{
		return _maxChestPerLoc;
	}

	public final int getSpawnRate()
	{
		return _spawnRate;
	}

	public final int getExpireRate()
	{
		return _expireRate;
	}

	public final int getSpawnRadius()
	{
		return _spawnRadius;
	}

	public final int getMaxActive()
	{
		return _maxActive;
	}

	public final Map<Integer, Integer> getSpawnIndexes()
	{
		return _spawnedIndexes;
	}

	public void setLastSpawn()
	{
		_lastSpawn = System.currentTimeMillis();
	}

	public long getLastSpawn()
	{
		return _lastSpawn;
	}

}
