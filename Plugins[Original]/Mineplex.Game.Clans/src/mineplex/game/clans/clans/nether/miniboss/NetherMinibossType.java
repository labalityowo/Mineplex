package mineplex.game.clans.clans.nether.miniboss;

import mineplex.game.clans.clans.nether.miniboss.bosses.ArcherMiniboss;
import mineplex.game.clans.clans.nether.miniboss.bosses.GhastMiniboss;
import mineplex.game.clans.clans.nether.miniboss.bosses.WarriorMiniboss;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

/**
 * Enum with all types of nether minibosses
 */
public enum NetherMinibossType
{
	GHAST("Ghast", 25D, EntityType.GHAST, GhastMiniboss.class),
	WARRIOR("Undead Warrior", 30D, EntityType.ZOMBIE, WarriorMiniboss.class),
	ARCHER("Undead Archer", 25D, EntityType.SKELETON, ArcherMiniboss.class)
	;
	
	private Class<? extends NetherMiniBoss<?>> _code;
	private String _name;
	private Double _maxHealth;
	private EntityType _type;
	
	private NetherMinibossType(String name, Double maxHealth, EntityType type, Class<? extends NetherMiniBoss<?>> code)
	{
		_name = name;
		_maxHealth = maxHealth;
		_type = type;
		_code = code;
	}
	
	/**
	 * Creates a new instance of this miniboss at a given location
	 * @param spawn The location to spawn the boss in
	 * @return The instance of the miniboss
	 */
	public NetherMiniBoss<?> getNewInstance(Location spawn)
	{
		try
		{
			return _code.getConstructor(String.class, Double.class, Location.class, EntityType.class).newInstance(_name, _maxHealth, spawn, _type);
		}
		catch (Exception e)
		{
			return null;
		}
	}
}