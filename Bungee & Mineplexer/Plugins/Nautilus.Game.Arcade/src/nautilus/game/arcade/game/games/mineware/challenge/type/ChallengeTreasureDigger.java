package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A PvP based challenge where players have to find weapons.
 */
public class ChallengeTreasureDigger extends Challenge
{
	private static final int CHALLENGE_PLAYERS_MIN = 4;
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_HEIGHT = 4;
	private static final int BEDROCK_LEVEL = 0;
	private static final int TREASURE_LEVEL = 1;
	private static final int RED_SANDSTONE_LEVEL = 2;
	private static final int SANDSTONE_LEVEL = 3;
	private static final int SAND_LEVEL = 4;

	private static final double TREASURE_CHANCE = 0.15;
	private static final double SANDSTONE_CHANCE = 0.1;
	private static final double SANDSTONE_CHANCE_TREASURE_LEVEL = 0.25;
	private static final double RED_SANDSTONE_CHANCE = 0.3;
	private static final double DEAD_BUSH_CHANCE = 0.015;

	private static final int SHOVEL_SLOT = 4;
	private static final int CHEST_DATA_RANGE = 4;
	private static final int CHEST_LOOT_AMOUNT_RANDOM = 2;
	private static final int CHEST_COSMETIC_ITEM_AMOUNT_RANDOM = 3;

	private Map<Material, Double> _lootChance = new LinkedHashMap<>();
	private Material[] _lootContents;

	public ChallengeTreasureDigger(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Treasure Digger",
			"Search for treasure below the sand.",
			"Find weapons to kill the others!");

		Settings.setUseMapHeight();
		Settings.setMinPlayers(CHALLENGE_PLAYERS_MIN);

		_lootChance.put(Material.BONE, 0.2);
		_lootChance.put(Material.STRING, 0.2);

		_lootChance.put(Material.WOOD_SWORD, 0.3);
		_lootChance.put(Material.STONE_SWORD, 0.2);
		_lootChance.put(Material.IRON_SWORD, 0.1);
		_lootChance.put(Material.GOLD_SWORD, 0.1);
		_lootChance.put(Material.DIAMOND_SWORD, 0.05);

		_lootChance.put(Material.IRON_SPADE, 0.15);
		_lootChance.put(Material.IRON_PICKAXE, 0.15);

		_lootChance.put(Material.GOLDEN_APPLE, 0.05);
		_lootChance.put(Material.FISHING_ROD, 0.1);
		_lootChance.put(Material.BOW, 0.2);
		_lootChance.put(Material.ARROW, 0.3);

		_lootChance.put(Material.LEATHER_HELMET, 0.2);
		_lootChance.put(Material.LEATHER_CHESTPLATE, 0.15);
		_lootChance.put(Material.LEATHER_LEGGINGS, 0.12);
		_lootChance.put(Material.LEATHER_BOOTS, 0.2);

		_lootChance.put(Material.CHAINMAIL_HELMET, 0.1);
		_lootChance.put(Material.CHAINMAIL_CHESTPLATE, 0.05);
		_lootChance.put(Material.CHAINMAIL_LEGGINGS, 0.07);
		_lootChance.put(Material.CHAINMAIL_BOOTS, 0.1);

		_lootChance.put(Material.IRON_HELMET, 0.1);
		_lootChance.put(Material.IRON_CHESTPLATE, 0.05);
		_lootChance.put(Material.IRON_LEGGINGS, 0.07);
		_lootChance.put(Material.IRON_BOOTS, 0.1);

		_lootChance.put(Material.DIAMOND_HELMET, 0.05);
		_lootChance.put(Material.DIAMOND_CHESTPLATE, 0.02);
		_lootChance.put(Material.DIAMOND_LEGGINGS, 0.04);
		_lootChance.put(Material.DIAMOND_BOOTS, 0.05);

		_lootContents = _lootChance.keySet().toArray(new Material[_lootChance.keySet().size()]);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (Location location : circle(getCenter(), size, 1, true, false, 0))
		{
			spawns.add(location.add(0, MAP_HEIGHT, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int i = 0; i <= MAP_HEIGHT; i++)
		{
			Location center = getCenter();

			if (i > 0)
			{
				center.add(0, i, 0);
			}

			for (Location location : circle(center, getArenaSize(), 1, false, false, 0))
			{
				Block block = location.getBlock();
				double chance = Math.random();

				if (i == BEDROCK_LEVEL)
				{
					setBlock(block, Material.BEDROCK);
				}
				else if (i == TREASURE_LEVEL)
				{
					if (chance < TREASURE_CHANCE)
					{
						makeChestWithTreasure(block);
					}
					else if (chance < SANDSTONE_CHANCE_TREASURE_LEVEL)
					{
						setBlock(block, Material.SANDSTONE);
					}
					else
					{
						setBlock(block, Material.SAND);
					}
				}
				else if (i == RED_SANDSTONE_LEVEL)
				{
					if (chance < SANDSTONE_CHANCE)
					{
						setBlock(block, Material.SANDSTONE);
					}
					else
					{
						setBlock(block, Material.SAND);
						chance = Math.random();

						if (chance < RED_SANDSTONE_CHANCE)
						{
							setData(block, (byte) 1);
						}
					}
				}
				else if (i == SANDSTONE_LEVEL)
				{
					if (chance < SANDSTONE_CHANCE)
					{
						setBlock(block, Material.SANDSTONE);
					}
					else
					{
						setBlock(block, Material.SAND);
					}
				}
				else if (i == SAND_LEVEL)
				{
					Block below = block.getRelative(BlockFace.DOWN);

					if (chance < DEAD_BUSH_CHANCE && below.getType() == Material.SAND)
					{
						setBlock(block, Material.DEAD_BUSH);
					}
				}

				addBlock(block);
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamagePvP = true;
		Host.InventoryOpenChest = true;
		Host.BlockBreak = true;
		Host.InventoryOpenBlock = true;
		Host.InventoryClick = true;
		Host.WorldBlockBurn = true;
		Host.WorldFireSpread = true;

		for (Player players : getPlayersAlive())
		{
			ItemStack shovel = new ItemStack(Material.STONE_SPADE);
			players.getInventory().setItem(SHOVEL_SLOT, shovel);
			players.getInventory().setHeldItemSlot(SHOVEL_SLOT);
		}
	}

	@Override
	public void onEnd()
	{
		Host.DamagePvP = true;
		Host.InventoryOpenChest = false;
		Host.BlockBreak = false;
		Host.InventoryOpenBlock = false;
		Host.InventoryClick = false;
		Host.DamagePvP = false;
		Host.WorldBlockBurn = false;
		Host.WorldFireSpread = false;

		remove(EntityType.DROPPED_ITEM);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!isPlayerValid(event.getPlayer()) || !Data.isModifiedBlock(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!Host.getDeathEffect().isDeathEffectItem(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof FallingBlock)
		{
			FallingBlock block = (FallingBlock) event.getEntity();
			block.setDropItem(false);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player)
		{
			if (!isPlayerValid((Player) event.getEntity()))
				return;

			if (!isPlayerValid((Player) event.getDamager()))
				return;

			Player damager = (Player) event.getDamager();
			ItemStack item = damager.getItemInHand();

			if (item != null)
			{
				if (!item.getType().name().toLowerCase().contains("sword"))
				{
					alert(damager, C.cRed + "You cannot attack without a weapon.");
					damager.playSound(damager.getLocation(), Sound.NOTE_BASS_GUITAR, 1.0F, 0.5F);
					event.setCancelled(true);
				}
			}
		}
	}

	private void makeChestWithTreasure(Block block)
	{
		if (areChestsNearby(block))
		{
			setBlock(block, Material.SAND);
		}
		else
		{
			setBlock(block, Material.CHEST, (byte) UtilMath.r(CHEST_DATA_RANGE));
			Chest chest = (Chest) block.getState();
			fillChestWithLoot(chest);
		}
	}

	private boolean areChestsNearby(Block block)
	{
		Block north = block.getRelative(BlockFace.NORTH);
		Block south = block.getRelative(BlockFace.SOUTH);
		Block east = block.getRelative(BlockFace.EAST);
		Block west = block.getRelative(BlockFace.WEST);

		return north.getType() == Material.CHEST || south.getType() == Material.CHEST || east.getType() == Material.CHEST || west.getType() == Material.CHEST;
	}

	private void fillChestWithLoot(Chest chest)
	{
		Inventory inv = chest.getInventory();

		for (int i = 0; i <= UtilMath.r(CHEST_LOOT_AMOUNT_RANDOM) + 1; i++)
		{
			double chance = Math.random();
			Material loot = getRandomLootMaterial();
			double lootChance = getLootChance(loot);

			while (chance >= lootChance)
			{
				chance = Math.random();
				loot = getRandomLootMaterial();
				lootChance = getLootChance(loot);
			}

			if (chance < lootChance)
			{
				ItemStack item = new ItemStack(loot);

				if (item.getType() == Material.ARROW || item.getType() == Material.BONE || item.getType() == Material.STRING)
				{
					item.setAmount(UtilMath.r(CHEST_COSMETIC_ITEM_AMOUNT_RANDOM) + 1);
				}

				int slot = UtilMath.r(inv.getSize());

				while (inv.getItem(slot) != null && inv.getContents().length != inv.getSize())
				{
					slot = UtilMath.r(inv.getSize());
				}

				inv.setItem(slot, item);
			}
		}
	}

	private Material getRandomLootMaterial()
	{
		Material loot = UtilMath.randomElement(_lootContents);
		return loot;
	}

	private double getLootChance(Material loot)
	{
		return _lootChance.get(loot);
	}
}
