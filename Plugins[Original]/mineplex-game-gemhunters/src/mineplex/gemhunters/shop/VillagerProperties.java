package mineplex.gemhunters.shop;

public class VillagerProperties
{

	private final String _name;
	private final String _dataKey;
	private final boolean _selling;
	private final int _spawnRate;
	private final int _expireRate;
	private final int _max;

	private long _lastSpawn;

	public VillagerProperties(String name, String dataKey, boolean selling, int spawnRate, int expireRate, int max)
	{
		_name = name;
		_dataKey = dataKey;
		_selling = selling;
		_spawnRate = spawnRate;
		_expireRate = expireRate;
		_max = max;

		setLastSpawn();
	}

	public final String getName()
	{
		return _name;
	}

	public final String getDataKey()
	{
		return _dataKey;
	}

	public boolean isSelling()
	{
		return _selling;
	}

	public final int getSpawnRate()
	{
		return _spawnRate;
	}

	public final int getExpireRate()
	{
		return _expireRate;
	}

	public final int getMax()
	{
		return _max;
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
