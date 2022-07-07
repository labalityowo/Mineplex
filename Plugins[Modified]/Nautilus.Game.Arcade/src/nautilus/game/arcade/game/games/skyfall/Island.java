package nautilus.game.arcade.game.games.skyfall;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.loot.ChestLoot;
import mineplex.core.titles.tracks.standard.LuckyTrack;
import mineplex.core.titles.tracks.standard.UnluckyTrack;

/**
 * The Island Object represents a flying Island <br/>
 * which has it's own chests and is able to crumble away.
 *
 * @author xXVevzZXx
 */
public class Island extends Crumbleable
{

	private Location _location;

	private ArrayList<Location> _chests;
	private ArrayList<Location> _blocks;

	private ArrayList<Location> _lootedBlocks;

	private int _bounds;
	private int _height;

	private ChestLoot _loot;

	private BoosterRing _boosterRing;

	/**
	 * @param location top middle location of the island
	 * @param bounds   how many blocks to go in each direction
	 * @param height   of the island
	 */
	public Island(Location location, int bounds, int height)
	{
		this(location, new ChestLoot(), bounds, height);
	}

	/**
	 * @param location top middle location of the island
	 * @param loot     prefered {@link LootTable}
	 * @param bounds   how many blocks to go in each direction
	 * @param height   of the island
	 */
	public Island(Location location, LootTable loot, int bounds, int height)
	{
		this(location, loot.getloot(), bounds, height);
	}

	/**
	 * @param location top middle location of the island
	 * @param loot     prefered {@link ChestLoot}
	 * @param bounds   how many blocks to go in each direction
	 * @param height   of the island
	 */
	public Island(Location location, ChestLoot loot, int bounds, int height)
	{
		super(true, height);
		
		_location = location;
		_bounds = bounds;
		_height = height;
		_chests = new ArrayList<>();
		_blocks = new ArrayList<>();
		_lootedBlocks = new ArrayList<>();
		_loot = loot;

		registerBlocks();
		init();
	}

	public void fillLoot(Block block)
	{
		if (block.getType() != Material.CHEST
				&& block.getType() != Material.TRAPPED_CHEST)
			return;
		
		if (_lootedBlocks.contains(block.getLocation()))
			return;
		
		_lootedBlocks.add(block.getLocation());
		
		Chest chest = (Chest) block.getState();
		Inventory inventory = chest.getBlockInventory();
		inventory.clear();
		
		int items = 5;
		if (Math.random() > 0.50)
			items++;
		if (Math.random() > 0.65)
			items++;
		if (Math.random() > 0.80)
			items++;
		if (Math.random() > 0.95)
			items++;
		
		ArrayList<Material> exclude = new ArrayList<>();
		for (int i = 0; i < items; i++)
		{
			int trys = 0;
			int slot = UtilMath.r(26);
			while (inventory.getItem(slot) != null && trys <= 5)
			{
				trys++;
				slot = UtilMath.r(26);
			}
			
			ItemStack item = _loot.getLoot(exclude);
			inventory.setItem(slot, item);
			if (item.getType() == Material.DIAMOND)
			{
				inventory.setItem(slot + 1, new ItemStack(Material.STICK, 2));
			}
			if (item.getType() == Material.BOW)
			{
				inventory.setItem(slot + 1, new ItemStack(Material.ARROW, UtilMath.r(6) + 1));
			}
			
			if (UtilItem.isHelmet(item))
			{
				exclude.add(Material.CHAINMAIL_HELMET);
				exclude.add(Material.GOLD_HELMET);
				exclude.add(Material.IRON_HELMET);
				exclude.add(Material.LEATHER_HELMET);
				exclude.add(Material.DIAMOND_HELMET);
			}
			if (UtilItem.isChestplate(item))
			{
				exclude.add(Material.CHAINMAIL_CHESTPLATE);
				exclude.add(Material.GOLD_CHESTPLATE);
				exclude.add(Material.IRON_CHESTPLATE);
				exclude.add(Material.LEATHER_CHESTPLATE);
				exclude.add(Material.DIAMOND_CHESTPLATE);	
			}
			if (UtilItem.isLeggings(item))
			{
				exclude.add(Material.CHAINMAIL_LEGGINGS);
				exclude.add(Material.GOLD_LEGGINGS);
				exclude.add(Material.IRON_LEGGINGS);
				exclude.add(Material.LEATHER_LEGGINGS);
				exclude.add(Material.DIAMOND_LEGGINGS);
			}
			if (UtilItem.isBoots(item))
			{
				exclude.add(Material.CHAINMAIL_BOOTS);
				exclude.add(Material.GOLD_BOOTS);
				exclude.add(Material.IRON_BOOTS);
				exclude.add(Material.LEATHER_BOOTS);
				exclude.add(Material.DIAMOND_BOOTS);
			}
			if (UtilItem.isSword(item))
			{
				exclude.add(Material.WOOD_SWORD);
				exclude.add(Material.STONE_SWORD);
				exclude.add(Material.IRON_SWORD);
				exclude.add(Material.DIAMOND_SWORD);
			}
			if (UtilItem.isAxe(item))
			{
				exclude.add(Material.WOOD_AXE);
				exclude.add(Material.STONE_AXE);
				exclude.add(Material.IRON_AXE);
				exclude.add(Material.DIAMOND_AXE);
			}
			if (item.getType() == Material.BOW)
				exclude.add(Material.BOW);
			
		}
	}

	public Location getLocation()
	{
		return _location;
	}

	public ArrayList<Location> getChests()
	{
		return _chests;
	}

	public int getBounds()
	{
		return _bounds;
	}

	public int getHeight()
	{
		return _height;
	}

	public boolean isOnIsland(Player player)
	{
		return isOnIsland(player.getLocation());
	}

	public boolean isOnIsland(Location location)
	{	
		if (UtilMath.offset2d(location, _location) > _bounds + 1)
			return false;
		
		for (int y = (_location.getBlockY() - _height); y <= _location.getBlockY(); y++)
		{
			if (location.getBlockY() == y)
				return true;
		}
		return false;
	}

	@Override
	public void crumbledAway()
	{
		if (_boosterRing != null)
		{
			if (!_boosterRing.isDisabled())
				_boosterRing.disable();
		}
	}

	public void registerBlocks()
	{
		for (Block block : UtilBlock.getInBoundingBox(_location.clone().add(_bounds, 0, _bounds), _location.clone().subtract(_bounds, _height, _bounds), false))
		{
			if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)
				getChests().add(block.getLocation());

			_blocks.add(block.getLocation());
		}
	}

	public void refillChests()
	{
		_lootedBlocks.clear();
		for (Location loc : _chests)
		{
			fillLoot(loc.getBlock());
		}
	}

	public void setBoosterRing(BoosterRing ring)
	{
		_boosterRing = ring;
	}

	public BoosterRing getBoosterRing()
	{
		return _boosterRing;
	}

	@Override
	public ArrayList<Location> getBlocks()
	{
		return _blocks;
	}
}
