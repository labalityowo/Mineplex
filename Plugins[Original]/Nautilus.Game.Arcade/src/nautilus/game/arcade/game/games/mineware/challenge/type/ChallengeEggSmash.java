package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.NumberTracker;

/**
 * A challenge based on dragon eggs.
 */
public class ChallengeEggSmash extends Challenge implements NumberTracker
{
	private static final int SCORE_GOAL = 10;
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;
	private static final int MAP_HEIGHT = 1;
	private static final double EFFECT_LOCATION_XYZ_ADD = 0.5;
	private static final int WOOL_DATA_RANGE = 16;

	private static final double FALLING_DRAGON_BLOCK_SPAWN_X_ADD = 0.5;
	private static final double FALLING_DRAGON_BLOCK_SPAWN_Y_ADD = 2;
	private static final double FALLING_DRAGON_BLOCK_SPAWN_Z_ADD = 0.5;

	private static final double SCORE_LOCATION_X_ADD = 0.5;
	private static final double SCORE_LOCATION_Y_ADD = 0.7;
	private static final double SCORE_LOCATION_Z_ADD = 0.5;

	private static final int SCORE_COLOR_CHANGE_1 = 1;
	private static final int SCORE_COLOR_CHANGE_2 = 2;
	private static final int SCORE_COLOR_CHANGE_3 = 3;
	private static final int SCORE_COLOR_CHANGE_4 = 4;
	private static final int SCORE_COLOR_CHANGE_5 = 5;
	private static final int SCORE_COLOR_CHANGE_6 = 6;
	private static final int SCORE_COLOR_CHANGE_7 = 7;
	private static final int SCORE_COLOR_CHANGE_8 = 8;
	private static final int SCORE_COLOR_CHANGE_9 = 9;

	private Map<Player, Integer> _score = new HashMap<>();

	public ChallengeEggSmash(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Egg Smash",
			"Smash " + SCORE_GOAL + " dragon eggs.");

		Settings.setUseMapHeight();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -(size); x < size; x++)
		{
			for (int z = -(size); z < size; z++)
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
		createFloorMapPart();
		createDragonEggsMapPart();
	}

	@Override
	public void onStart()
	{
		ItemStack axe = new ItemBuilder(Material.IRON_AXE)
			.setUnbreakable(true)
			.setItemFlags(ItemFlag.HIDE_UNBREAKABLE)
			.build();

		for (Player player : getPlayersAlive())
		{
			_score.put(player, 0);
			player.getInventory().setItem(Settings.getLockedSlot(), axe);
		}
	}

	@Override
	public void onEnd()
	{
		_score.clear();
		remove(EntityType.FALLING_BLOCK);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		Action action = event.getAction();

		if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();

			if (block.getType() == Material.DRAGON_EGG)
			{
				block.getWorld().playEffect(block.getLocation().add(EFFECT_LOCATION_XYZ_ADD, EFFECT_LOCATION_XYZ_ADD, EFFECT_LOCATION_XYZ_ADD), Effect.STEP_SOUND, block.getTypeId());
				resetBlock(block);

				spawnDragonEgg();
				showAndIncrementScore(player, block);

				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof FallingBlock)
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!isChallengeValid())
					{
						cancel();
						return;
					}

					addBlock(event.getBlock());
				}
			}.runTaskLater(Host.getArcadeManager().getPlugin(), 1L);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		_score.remove(event.getPlayer());
	}

	private void createFloorMapPart()
	{
		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				Block block = getCenter().getBlock().getRelative(x, 0, z);
				setBlock(block, Material.WOOL, (byte) UtilMath.r(WOOL_DATA_RANGE));

				addBlock(block);
			}
		}
	}

	private static final int DRAGON_EGG_AMOUNT = 9;

	private void createDragonEggsMapPart()
	{
		for (int i = 0; i < DRAGON_EGG_AMOUNT; i++)
		{
			Block block = getRandomBlock();
			setBlock(block, Material.DRAGON_EGG);
			addBlock(block);
		}
	}

	@SuppressWarnings("deprecation")
	private void spawnDragonEgg()
	{
		Host.CreatureAllow = true;

		for (int i = 0; i < 10; i++)
		{
			Block block = getRandomBlock();

			if (block.isEmpty())
			{
				getCenter().getWorld().spawnFallingBlock(block.getLocation().add(FALLING_DRAGON_BLOCK_SPAWN_X_ADD, FALLING_DRAGON_BLOCK_SPAWN_Y_ADD, FALLING_DRAGON_BLOCK_SPAWN_Z_ADD), Material.DRAGON_EGG, (byte) 0);
				break;
			}
		}

		Host.CreatureAllow = false;
	}

	private static final int DRAGON_SPAWN_LOCATION_MULTIPLIER = 2;

	private Block getRandomBlock()
	{
		return getCenter().add(
			UtilMath.r((getArenaSize() * DRAGON_SPAWN_LOCATION_MULTIPLIER)) - getArenaSize(),
			1,
			UtilMath.r((getArenaSize() * DRAGON_SPAWN_LOCATION_MULTIPLIER)) - getArenaSize()).getBlock();
	}

	private void showAndIncrementScore(Player player, Block block)
	{
		int score = _score.get(player) + 1;

		displayCount(player, block.getLocation().add(SCORE_LOCATION_X_ADD, SCORE_LOCATION_Y_ADD, SCORE_LOCATION_Z_ADD), selectScoreColor(score));

		_score.put(player, score);

		if (score == SCORE_GOAL)
		{
			setCompleted(player);
		}
	}

	private String selectScoreColor(int score)
	{
		if (score == SCORE_COLOR_CHANGE_1 || score == SCORE_COLOR_CHANGE_2)
		{
			return C.cWhiteB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_3 || score == SCORE_COLOR_CHANGE_4)
		{
			return C.cGreenB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_5 || score == SCORE_COLOR_CHANGE_6)
		{
			return C.cYellowB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_7 || score == SCORE_COLOR_CHANGE_8)
		{
			return C.cGoldB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_9)
		{
			return C.cRedB + score;
		}
		else
		{
			return C.cDRedB + score;
		}
	}

	@Override
	public Number getData(Player player)
	{
		return _score.get(player);
	}

	@Override
	public boolean hasData(Player player)
	{
		return _score.containsKey(player);
	}
}
