package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on finding diamonds.
 */
public class ChallengeDiamondHunt extends Challenge
{
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int SPAWN_COORDINATES_MULTIPLE = 2;
	private static final int MAP_HEIGHT = 2;
	private static final int STAINED_CLAY_DATA_RANGE = 16;
	private static final int CHEST_DATA_RANGE = 4;
	private static final double CHEST_INVENTORY_SLOT_FILL_CHANCE = 0.7;
	private static final int DIAMOND_AMOUNT_MIN = 10;

	private static final Material[] CHEST_MATERIALS = {
		Material.WOOD_SPADE,
		Material.WOOD_PICKAXE,
		Material.WOOD_AXE,
		Material.WOOD_HOE,
		Material.STONE_SPADE,
		Material.STONE_PICKAXE,
		Material.STONE_AXE,
		Material.STONE_HOE,
		Material.IRON_SPADE,
		Material.IRON_PICKAXE,
		Material.IRON_AXE,
		Material.IRON_HOE,
		Material.GOLD_SPADE,
		Material.GOLD_PICKAXE,
		Material.GOLD_AXE,
		Material.GOLD_HOE,
		Material.DIAMOND_SPADE,
		Material.DIAMOND_PICKAXE,
		Material.DIAMOND_AXE,
		Material.DIAMOND_HOE,
		Material.WOOD_SWORD,
		Material.STONE_SWORD,
		Material.IRON_SWORD,
		Material.GOLD_SWORD,
		Material.DIAMOND_SWORD,

		Material.SNOW_BALL,
		Material.PAPER,
		Material.SLIME_BALL,
		Material.BONE,
		Material.ENDER_PEARL,
		Material.EYE_OF_ENDER,
		Material.COAL,
		Material.IRON_INGOT,
		Material.GOLD_INGOT,
		Material.EMERALD,
		Material.STICK,
		Material.STRING,
		Material.BOWL,
		Material.FEATHER,
		Material.SEEDS,
		Material.MELON_SEEDS,
		Material.PUMPKIN_SEEDS,
		Material.SUGAR_CANE,
		Material.WHEAT,
		Material.BRICK,
		Material.NETHER_STALK,
		Material.EGG,
		Material.FLINT,
		Material.LEATHER,
		Material.GOLD_NUGGET,
		Material.NETHER_BRICK,
		Material.FISHING_ROD,
		Material.CARROT_STICK,
		Material.SHEARS,
		Material.LEASH,
		Material.REDSTONE,
		Material.GHAST_TEAR,
		Material.IRON_BARDING,
		Material.GOLD_BARDING,
		Material.DIAMOND_BARDING,
		Material.TRIPWIRE_HOOK,

		Material.STONE,
		Material.DIAMOND_ORE,
		Material.DIAMOND_BLOCK,

		Material.APPLE,
		Material.BREAD,
		Material.ROTTEN_FLESH,
		Material.GOLDEN_APPLE
	};

	public ChallengeDiamondHunt(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Diamond Hunt",
			"Find a diamond in the chests.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				if (x % SPAWN_COORDINATES_MULTIPLE == 0 && z % SPAWN_COORDINATES_MULTIPLE == 0)
				{
					spawns.add(getCenter().add(x, MAP_HEIGHT, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		ArrayList<Inventory> inventories = new ArrayList<Inventory>();

		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				Block block = getCenter().getBlock().getRelative(x, 0, z);
				setBlock(block, Material.STAINED_CLAY, (byte) UtilMath.r(STAINED_CLAY_DATA_RANGE));
				addBlock(block);

				if (Math.abs(x) % SPAWN_COORDINATES_MULTIPLE == 0 && Math.abs(z) % SPAWN_COORDINATES_MULTIPLE == 0)
				{
					Block relativeBlock = block.getRelative(0, 1, 0);
					setBlock(relativeBlock, Material.CHEST, (byte) UtilMath.r(CHEST_DATA_RANGE));
					addBlock(relativeBlock);

					Inventory inventory = ((Chest) relativeBlock.getState()).getInventory();
					inventories.add(inventory);

					for (int i = 0; i < inventory.getSize(); i++)
					{
						ItemStack item = new ItemStack(UtilMath.randomElement(CHEST_MATERIALS));

						if (Math.random() < CHEST_INVENTORY_SLOT_FILL_CHANCE)
						{
							inventory.setItem(i, item);
						}
					}
				}
				else
				{
					Block relativeBlock = block.getRelative(BlockFace.UP);
					setBlock(relativeBlock, Material.STAINED_CLAY, (byte) UtilMath.r(STAINED_CLAY_DATA_RANGE));
					addBlock(relativeBlock);
				}
			}
		}

		for (int i = 0; i < DIAMOND_AMOUNT_MIN + Host.getPlayersWithRemainingLives(); i++)
		{
			Inventory inventory = UtilMath.randomElement(inventories);
			inventory.setItem(UtilMath.r(inventory.getSize()), new ItemStack(Material.DIAMOND));
		}
	}

	@Override
	public void onStart()
	{
		Host.InventoryOpenChest = true;
		Host.InventoryOpenBlock = true;
	}

	@Override
	public void onEnd()
	{
		Host.InventoryOpenChest = false;
		Host.InventoryOpenBlock = false;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (getPlayersAlive().contains(player) && Host.IsAlive(player) && Data.isDone(player))
		{
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
			{
				Block block = event.getClickedBlock();

				if (block == null)
					return;

				if (block.getType() == Material.CHEST)
				{
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (!isChallengeValid())
			return;

		InventoryHolder holder = event.getInventory().getHolder();
		Player player = (Player) event.getWhoClicked();

		if (!(holder instanceof Player))
		{
			if (!isPlayerValid(player))
				return;

			event.setCancelled(true);

			if (holder instanceof DoubleChest || holder instanceof Chest)
			{
				ItemStack item = event.getCurrentItem();

				if (item != null && item.getType() == Material.DIAMOND)
				{
					event.setCurrentItem(new ItemStack(Material.AIR));
					setCompleted(player);
					player.closeInventory();
				}
			}
		}
	}
}
