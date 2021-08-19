package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on finding and mining diamond ores.
 */
public class ChallengeOreRun extends Challenge
{
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 2;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;

	private static final byte DIRT_DATA = 2;
	private static final int DIAMOND_AMOUNT_DIVIDER = 2;
	private static final int DIAMOND_SPAWN_MULTIPLIER = 2;

	private static final int BOTTOM_LEVEL = 0;
	private static final int ORE_LEVEL = 1;
	private static final int FENCE_LEVEL = 2;

	private static final List<Material> ORES = new ArrayList<>(Arrays.asList(
		Material.COAL_ORE,
		Material.IRON_ORE,
		Material.GOLD_ORE,
		Material.EMERALD_ORE,
		Material.REDSTONE_ORE,
		Material.LAPIS_ORE));

	private List<Block> _diamonds = new ArrayList<>();

	public ChallengeOreRun(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Ore Run",
			"Find and mine a diamond around the map.");

		Settings.setUseMapHeight();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -getArenaSize() + MAP_SPAWN_SHIFT; x <= getArenaSize() - MAP_SPAWN_SHIFT; x++)
		{
			for (int z = -getArenaSize() + MAP_SPAWN_SHIFT; z <= getArenaSize() - MAP_SPAWN_SHIFT; z++)
			{
				if (x % SPAWN_COORDINATE_MULTIPLE == 0 && z % SPAWN_COORDINATE_MULTIPLE == 0)
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
		int amountOfDiamonds = 0;

		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					double absX = Math.abs(x);
					double absZ = Math.abs(z);
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == BOTTOM_LEVEL)
					{
						setBlock(block, Material.DIRT, DIRT_DATA);
					}
					else if (y == ORE_LEVEL)
					{
						if (absX == getArenaSize() || absZ == getArenaSize())
						{
							if (UtilMath.random.nextBoolean())
							{
								setBlock(block, Material.STONE);
							}
							else
							{
								setBlock(block, Material.COBBLESTONE);
							}
						}
						else
						{
							if (amountOfDiamonds == 0)
							{
								for (int i = 0; i < Math.ceil((Host.getPlayersWithRemainingLives() + 1) / DIAMOND_AMOUNT_DIVIDER); i++)
								{
									Block copy = getCenter().getBlock().getRelative(UtilMath.r(getArenaSize() * DIAMOND_SPAWN_MULTIPLIER) - (getArenaSize()), 1, UtilMath.r(getArenaSize() * DIAMOND_SPAWN_MULTIPLIER) - (getArenaSize()));

									if (copy.getType() == Material.DIAMOND_ORE && Math.abs(copy.getX()) < getArenaSize() && Math.abs(copy.getY()) < getArenaSize())
									{
										i--;
										continue;
									}

									_diamonds.add(copy);
									addBlock(copy);

									amountOfDiamonds++;
								}
							}

							if (block.getType() != Material.DIAMOND_ORE)
							{
								setBlock(block, UtilMath.randomElement(ORES));
							}
						}
					}
					else if (y == FENCE_LEVEL && (absX == getArenaSize() || absZ == getArenaSize()))
					{
						setBlock(block, Material.FENCE);
					}

					addBlock(block);
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		ItemStack pickaxe = new ItemBuilder(Material.DIAMOND_PICKAXE)
			.setUnbreakable(true)
			.setItemFlags(ItemFlag.HIDE_UNBREAKABLE)
			.build();

		setItem(Settings.getLockedSlot(), pickaxe);

		for (Block diamond : _diamonds)
		{
			setBlock(diamond, Material.DIAMOND_ORE);
		}

		Host.BlockBreak = true;
	}

	@Override
	public void onEnd()
	{
		Host.BlockBreak = false;
		_diamonds.clear();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (Data.isCompleted(player))
		{
			event.setCancelled(true);
			return;
		}

		if (!isPlayerValid(player))
			return;

		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();

			if (block == null)
				return;

			if (block.getType() == Material.DIAMOND_ORE)
			{
				setCompleted(player, true);
				resetBlock(block);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getBlock().getType() != Material.DIAMOND_ORE)
			event.setCancelled(true);
	}
}
