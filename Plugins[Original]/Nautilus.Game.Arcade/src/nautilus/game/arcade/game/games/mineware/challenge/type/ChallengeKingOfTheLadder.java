package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on climbling ladders.
 */
public class ChallengeKingOfTheLadder extends Challenge
{
	private static final int CHALLENGE_PLAYERS_MAX = 15;
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 1;

	private static final int LADDER_HEIGHT = 20;
	private static final byte LADDER_NORTH_DATA = 2;
	private static final byte LADDER_SOUTH_DATA = 3;
	private static final byte LADDER_WEST_DATA = 4;
	private static final byte LADDER_EAST_DATA = 5;

	// Coordinates relative to map center.
	private static final int WIN_CORNER_A_X = 1;
	private static final int WIN_CORNER_A_Y = 20;
	private static final int WIN_CORNER_A_Z = -1;
	private static final int WIN_CORNER_B_X = 0;
	private static final int WIN_CORNER_B_Y = 23;
	private static final int WIN_CORNER_B_Z = 1;

	private static final int STICK_KNOCKBACK_LEVEL = 5;
	private static final int PLAYER_MAX_HEALTH = 20;

	private Location _winCornerA;
	private Location _winCornerB;

	public ChallengeKingOfTheLadder(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"King of the Ladder",
			"Reach the top of the ladder.");

		Settings.setUseMapHeight();
		Settings.setMaxPlayers(CHALLENGE_PLAYERS_MAX);
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -(size); x <= size; x++)
		{
			for (int z = -(size); z <= size; z++)
			{
				if (Math.abs(x) == size || Math.abs(z) == size)
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
		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						setBlock(block, Material.GRASS);

						if (x == 0 && z == 0)
						{
							for (int h = 1; h <= LADDER_HEIGHT; h++)
							{
								Block ladder = getCenter().getBlock().getRelative(x, h, z);
								setBlock(ladder, Material.STONE);
								setBlock(ladder.getRelative(BlockFace.NORTH), Material.LADDER, LADDER_NORTH_DATA);
								setBlock(ladder.getRelative(BlockFace.SOUTH), Material.LADDER, LADDER_SOUTH_DATA);
								setBlock(ladder.getRelative(BlockFace.WEST), Material.LADDER, LADDER_WEST_DATA);
								setBlock(ladder.getRelative(BlockFace.EAST), Material.LADDER, LADDER_EAST_DATA);
							}
						}
					}
					else
					{
						if (Math.abs(x) == getArenaSize() || Math.abs(z) == getArenaSize())
						{
							setBlock(block, Material.FENCE);
						}
						else if (x != 0 && z != 0)
						{
							generateGrass(block);
						}
					}

					addBlock(block);

				}
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamagePvP = true;

		_winCornerA = getCenter().add(WIN_CORNER_A_X, WIN_CORNER_A_Y, WIN_CORNER_A_Z);
		_winCornerB = getCenter().add(WIN_CORNER_B_X, WIN_CORNER_B_Y, WIN_CORNER_B_Z);

		ItemStack stick = new ItemBuilder(Material.STICK)
			.addEnchantment(Enchantment.KNOCKBACK, STICK_KNOCKBACK_LEVEL)
			.addItemFlags(ItemFlag.HIDE_ENCHANTS)
			.build();

		setItem(Settings.getLockedSlot(), stick);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnd()
	{
		Host.DamagePvP = false;

		for (int h = 1; h <= LADDER_HEIGHT; h++)
		{
			Block block = getCenter().getBlock().getRelative(0, h, 0);
			Block north = block.getRelative(BlockFace.NORTH);
			Block south = block.getRelative(BlockFace.SOUTH);
			Block east = block.getRelative(BlockFace.EAST);
			Block west = block.getRelative(BlockFace.WEST);

			if (north.getType() == Material.LADDER && south.getType() == Material.LADDER && east.getType() == Material.LADDER && west.getType() == Material.LADDER)
			{
				resetBlock(north);
				resetBlock(south);
				resetBlock(east);
				resetBlock(west);
			}

			if (UtilMath.random.nextBoolean())
			{
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
			}

			resetBlock(block);
		}
	}

	@EventHandler
	public void onWinnerCheck(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!isChallengeValid())
			return;

		for (Player player : getPlayersAlive())
		{
			if (UtilAlg.inBoundingBox(player.getLocation(), _winCornerA, _winCornerB))
			{
				setCompleted(player, true);
			}
		}
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.GetDamageePlayer() == null)
			return;

		if (!isPlayerValid(event.GetDamagerPlayer(false)))
		{
			event.SetCancelled("Player already completed");
			return;
		}

		if (event.GetCause() == DamageCause.FALL)
		{
			event.SetCancelled("Fall damage");
		}
	}

	@EventHandler
	public void onResetPlayerHealth(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			for (Player player : getPlayersAlive())
			{
				player.setHealth(PLAYER_MAX_HEALTH);
			}
		}
	}
}
