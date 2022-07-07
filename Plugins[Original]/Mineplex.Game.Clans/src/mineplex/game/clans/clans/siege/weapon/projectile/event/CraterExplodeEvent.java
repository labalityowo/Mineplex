package mineplex.game.clans.clans.siege.weapon.projectile.event;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.siege.weapon.SiegeWeapon;

/**
 * Event called when a Crater explodes
 */
public class CraterExplodeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private SiegeWeapon _weapon;
	private Player _shooter;
	private Location _origin;
	private List<Block> _blocks;
	
	public CraterExplodeEvent(SiegeWeapon weapon, Player shooter, Location explosionOrigin, List<Block> blocks)
	{
		_weapon = weapon;
		_shooter = shooter;
		_origin = explosionOrigin;
		_blocks = blocks;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
	/**
	 * Gets the Siege Weapon that shot the projectile which created this crater
	 * @return The Siege Weapon that shot the projectile which created this crater
	 */
	public SiegeWeapon getWeapon()
	{
		return _weapon;
	}
	
	/**
	 * Gets the player responsible for this crater
	 * @return The player responsible for this crater
	 */
	public Player getShooter()
	{
		return _shooter;
	}
	
	/**
	 * Gets the location where this explosion originated
	 * @return The origin point of this explosion
	 */
	public Location getExplosionOrigin()
	{
		return _origin;
	}
	
	/**
	 * Gets a list of all blocks affected by this explosion
	 * @return A list of all blocks affected by this explosion
	 */
	public List<Block> getBlocks()
	{
		return _blocks;
	}
}