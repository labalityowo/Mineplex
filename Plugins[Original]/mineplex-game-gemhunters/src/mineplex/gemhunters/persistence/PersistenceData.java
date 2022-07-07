package mineplex.gemhunters.persistence;

import mineplex.gemhunters.quest.QuestPlayerData;
import mineplex.serverdata.Region;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PersistenceData
{

	private final Region _region;
	private final int _gems;
	private final Location _location;
	private final QuestPlayerData _questData;
	private final int _health;
	private final int _maxHealth;
	private final int _hunger;
	private final int _slots;
	private final ItemStack[] _items;
	private final ItemStack[] _armour;
	private final long _saveTime;
	private final int _cashOutTime;

	public PersistenceData(Region region, int gems, Location location, QuestPlayerData questData, int health, int maxHealth, int hunger, int slots, ItemStack[] items, ItemStack[] armour, long saveTime, int cashOutTime)
	{
		_region = region;
		_gems = gems;
		_location = location;
		_questData = questData;
		_health = health;
		_maxHealth = maxHealth;
		_hunger = hunger;
		_items = items;
		_slots = slots;
		_armour = armour;
		_saveTime = saveTime;
		_cashOutTime = cashOutTime;
	}

	public Region getRegion()
	{
		return _region;
	}

	public int getGems()
	{
		return _gems;
	}

	public Location getLocation()
	{
		return _location;
	}

	public QuestPlayerData getQuestData()
	{
		return _questData;
	}

	public int getHealth()
	{
		return _health;
	}

	public int getMaxHealth()
	{
		return _maxHealth;
	}

	public int getHunger()
	{
		return _hunger;
	}

	public int getSlots()
	{
		return _slots;
	}

	public ItemStack[] getItems()
	{
		return _items;
	}

	public ItemStack[] getArmour()
	{
		return _armour;
	}

	public long getSaveTime()
	{
		return _saveTime;
	}

	public int getCashOutTime()
	{
		return _cashOutTime;
	}
}
