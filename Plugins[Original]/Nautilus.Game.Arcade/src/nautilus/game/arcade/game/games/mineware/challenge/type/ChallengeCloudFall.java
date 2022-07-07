package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilShapes;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on dropping from a platform in the correct block.
 * 
 * @deprecated
 */
public class ChallengeCloudFall extends Challenge
{
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_HEIGHT = 85;
	private static final int MAP_SIZE_HOLLOW_DIVIDER = 2;
	private static final int WOOL_DATA_RANGE = 16;

	private static final double OBSTANCE_SPAWN_CHANCE = 0.005;
	private static final int OBSTACLE_MAX_HEIGHT = 70;
	private static final int OBSTANCE_DISTANCE_FROM_GROUND = 10;
	private static final byte OBSTANCE_COLOR_1 = 0;
	private static final byte OBSTANCE_COLOR_2 = 8;
	private static final double OBSTANCE_COLOR_CHANGE_CHANCE = 0.3;

	private static final Material LANDING_PLATFORM_BLOCK_TYPE = Material.WOOL;
	private static final byte LANDING_PLATFORM_BLOCK_DATA = 5;
	private static final int LANDING_PLATFORMS_HEIGHT = 2;
	private static final double LANDING_PLATFORM_SPAWN_CHANCE = 0.1;

	public ChallengeCloudFall(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Cloud Fall",
			"Jump and land on green wool.",
			"Avoid the deadly clouds!");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (Location location : circle(getCenter(), size, 1, true, false, MAP_HEIGHT))
		{
			spawns.add(location.add(0, 1, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		createBottomMiddleMapPart();
		createPlatformMapPart();
	}

	@Override
	public void onStart()
	{
		Host.DamageFall = true;
	}

	@Override
	public void onEnd()
	{
		Host.DamageFall = false;
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.GetCause() != DamageCause.FALL)
			return;

		Player player = event.GetDamageePlayer();

		if (!isPlayerValid(player))
			return;

		Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (below.getY() == (int) getCenter().getY() + LANDING_PLATFORMS_HEIGHT)
		{
			if (below.isEmpty())
			{
				Block[] nearby = {
					below.getRelative(BlockFace.NORTH),
					below.getRelative(BlockFace.EAST),
					below.getRelative(BlockFace.SOUTH),
					below.getRelative(BlockFace.WEST),
					below.getRelative(BlockFace.NORTH_EAST),
					below.getRelative(BlockFace.NORTH_WEST),
					below.getRelative(BlockFace.SOUTH_EAST),
					below.getRelative(BlockFace.SOUTH_WEST)
				};

				for (Block near : nearby)
				{
					if (isLandingBlock(near))
					{
						setCompleted(player);
						event.SetCancelled("Fell on wool");
						break;
					}
				}
			}
			else if (isLandingBlock(below))
			{
				setCompleted(player);
				event.SetCancelled("Fell on wool");
			}
			else
			{
				event.AddMod("Fell into another block", player.getHealth());
			}
		}
		else
		{
			event.AddMod("Fell into another block", player.getHealth());
		}
	}

	private boolean isLandingBlock(Block block)
	{
		return block.getType() == LANDING_PLATFORM_BLOCK_TYPE && block.getData() == LANDING_PLATFORM_BLOCK_DATA;
	}

	private void createBottomMiddleMapPart()
	{
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= OBSTACLE_MAX_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						setBlock(block, Material.WOOL);
					}
					else if (y == 1 && Math.random() < LANDING_PLATFORM_SPAWN_CHANCE && Math.abs(x) < size && Math.abs(z) < size)
					{
						Block upperBlock = block.getRelative(BlockFace.UP);
						createLandingWool(block, upperBlock);
						addBlock(upperBlock);
					}
					else if (canCreateObstacle(block) && y > OBSTANCE_DISTANCE_FROM_GROUND && Math.random() < OBSTANCE_SPAWN_CHANCE)
					{
						addBlock(createObstacle(block));
					}

					addBlock(block);
				}
			}
		}
	}

	private void createPlatformMapPart()
	{
		Location platform = getCenter().add(0, MAP_HEIGHT, 0);

		for (Location location : UtilShapes.getCircle(platform, false, getArenaSize()))
		{
			Block block = location.getBlock();
			setBlock(block, Material.WOOL, (byte) UtilMath.r(WOOL_DATA_RANGE));
			addBlock(block);
		}

		for (Location location : UtilShapes.getCircle(platform, false, (int) getArenaSize() / MAP_SIZE_HOLLOW_DIVIDER))
		{
			resetBlock(location.getBlock());
		}
	}

	private void createLandingWool(Block bottom, Block top)
	{
		setBlock(bottom, Material.WOOL, LANDING_PLATFORM_BLOCK_DATA);
		setBlock(top, Material.WOOL, LANDING_PLATFORM_BLOCK_DATA);
	}

	private boolean canCreateObstacle(Block center)
	{
		Block[] area = {
			center.getRelative(BlockFace.DOWN),
			center.getRelative(BlockFace.NORTH),
			center.getRelative(BlockFace.EAST),
			center.getRelative(BlockFace.SOUTH),
			center.getRelative(BlockFace.WEST),
			center.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN),
			center.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN),
			center.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN),
			center.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN)
		};

		boolean available = true;

		for (Block part : area)
		{
			if (!part.isEmpty())
			{
				available = false;
				break;
			}
		}

		return available && center.isEmpty();
	}

	private Block[] createObstacle(Block center)
	{
		Block north = center.getRelative(BlockFace.NORTH);
		Block east = center.getRelative(BlockFace.EAST);
		Block south = center.getRelative(BlockFace.SOUTH);
		Block west = center.getRelative(BlockFace.WEST);

		byte data = OBSTANCE_COLOR_1;

		if (Math.random() < OBSTANCE_COLOR_CHANGE_CHANCE)
		{
			data = OBSTANCE_COLOR_2;
		}

		setBlock(north, Material.WOOL, data);
		setBlock(east, Material.WOOL, data);
		setBlock(west, Material.WOOL, data);
		setBlock(south, Material.WOOL, data);

		return new Block[] { north, east, west, south };
	}
}
