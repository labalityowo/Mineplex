package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.LogicTracker;

/**
 * A challenge based on jumping and hitting block targets.
 */
public class ChallengeBouncingBlock extends Challenge implements LogicTracker
{
	private static final int SCORE_GOAL = 10;

	private static final int MAP_MIN_SIZE = 9;
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_HEIGHT = 1;

	private static final byte PLATFORM_BLOCK_DATA = 0;
	private static final int JUMP_EFFECT_MULTIPLIER = 6;

	private static final double TRAP_SPAWN_CHANCE = 0.2;
	private static final byte TRAP_BLOCK_DATA = 14;
	private static final int TRAP_SCORE_LOSS_MAX = 2;
	private static final int TRAP_SCORE_LOSS_MIN = 1;

	private static final float SCORE_SOUND_VOLUME = 0.2F;
	private static final float SCORE_SOUND_PITCH = 0.2F;
	private static final int SCORE_BLOCK_HEIGHT = 6;
	private static final int SCORE_BLOCK_HEIGHT_ADD = 5;
	private static final int SCORE_BLOCK_DATA_RANGE = 16;
	private static final int SCORE_BLOCK_SPAWN_SHIFT = 2;
	private static final int SCORE_BLOCK_BOUND_MULTIPLY = 2;
	private static final double SCORE_FIREWORK_LOCATION_ADD = 0.5;

	private static final int MILLISECONDS_UNTIL_NEXT_SCORE_LOSS = 300;

	private List<Player> _stepTracker = new ArrayList<>();
	private List<Block> _blocks = new ArrayList<>();
	private Map<Player, Integer> _score = new HashMap<>();

	public ChallengeBouncingBlock(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Bouncing Block",
			"Jump and punch floating wool blocks.",
			"Avoid landing on red wool.",
			"Get to " + SCORE_GOAL + " to win!");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize(MAP_MIN_SIZE) - MAP_SPAWN_SHIFT;

		for (Location location : circle(getCenter(), size, 1, true, false, 0))
		{
			spawns.add(location.add(0, MAP_HEIGHT, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (Location location : circle(getCenter(), getArenaSize(MAP_MIN_SIZE), 1, false, false, 0))
		{
			Block block = location.getBlock();
			setBlock(block, Material.WOOL);

			if (Math.random() < TRAP_SPAWN_CHANCE)
			{
				setData(block, (byte) TRAP_BLOCK_DATA);
			}
			else
			{
				setData(block, (byte) PLATFORM_BLOCK_DATA);
			}

			addBlock(block);
		}

		spawnStartingWool();
	}

	@Override
	public void onStart()
	{
		addEffect(PotionEffectType.JUMP, JUMP_EFFECT_MULTIPLIER);

		for (Player player : getPlayersAlive())
		{
			_score.put(player, 0);
		}
	}

	@Override
	public void onEnd()
	{
		_stepTracker.clear();

		for (Block woolBlock : _blocks)
		{
			resetBlock(woolBlock);
		}

		_blocks.clear();
		_score.clear();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)
			return;

		Block block = event.getClickedBlock();

		if (!_blocks.contains(block))
			return;

		hitBlock(player, block);
		checkCompleted(player);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : getPlayersIn(false))
		{
			UtilTextBottom.display(C.Bold + "Score: " + C.Reset + C.cYellow + C.Bold + _score.get(player), player);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (isLooking(event.getFrom(), event.getTo()))
			return;

		Location loc = player.getLocation();
		Block blockBelow = loc.getBlock().getRelative(BlockFace.DOWN);

		if (canLooseScore(player) && blockBelow.getType() == Material.WOOL)
		{
			Wool woolBelow = (Wool) blockBelow.getState().getData();

			if (woolBelow.getColor() == DyeColor.RED)
			{
				int amount = subtractFromScore(player);
				showSubtractMessage(player, amount);
				trackStep(player);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		_stepTracker.remove(player);
		_score.remove(player);
	}

	private void hitBlock(Player player, Block block)
	{
		increment(player, 1);
		player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, SCORE_SOUND_VOLUME, SCORE_SOUND_PITCH);
		resetBlock(block);
		spawnRandomWool(true);
	}

	private void checkCompleted(Player player)
	{
		if (_score.get(player) >= SCORE_GOAL)
		{
			setCompleted(player);
		}
	}

	private boolean isLooking(Location from, Location to)
	{
		return from.getX() == to.getX() && from.getZ() == to.getZ();
	}

	private boolean canLooseScore(Player player)
	{
		return Recharge.Instance.use(player, "Score Loss", MILLISECONDS_UNTIL_NEXT_SCORE_LOSS, false, false) && _score.get(player) > 0;
	}

	private int subtractFromScore(Player player)
	{
		int amount = TRAP_SCORE_LOSS_MAX;

		if (_score.get(player) == 1)
		{
			amount = TRAP_SCORE_LOSS_MIN;
		}

		subtract(player, amount);
		return amount;
	}

	private void showSubtractMessage(Player player, int amount)
	{
		alert(player, "Score decreased by " + C.cRed + amount + C.Reset + "!");
		player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
	}

	private void trackStep(Player player)
	{
		if (!_stepTracker.contains(player))
		{
			_stepTracker.add(player);
		}
	}

	private void spawnStartingWool()
	{
		for (int i = 0; i < Host.getPlayersWithRemainingLives() + 1; i++)
		{
			spawnRandomWool(false);
		}
	}

	private void spawnRandomWool(boolean firework)
	{
		int size = getArenaSize(MAP_MIN_SIZE) - SCORE_BLOCK_SPAWN_SHIFT;

		int x = UtilMath.r(size * SCORE_BLOCK_BOUND_MULTIPLY) - size;
		int y = SCORE_BLOCK_HEIGHT + UtilMath.r(SCORE_BLOCK_HEIGHT_ADD);
		int z = UtilMath.r(size * SCORE_BLOCK_BOUND_MULTIPLY) - size;

		while (!Host.WorldData.World.getBlockAt(x, y, z).isEmpty())
		{
			x = UtilMath.r(size * SCORE_BLOCK_BOUND_MULTIPLY) - size;
			y = SCORE_BLOCK_HEIGHT + UtilMath.r(SCORE_BLOCK_HEIGHT_ADD);
			z = UtilMath.r(size * SCORE_BLOCK_BOUND_MULTIPLY) - size;
		}

		spawnRandomWoolAt(x, y, z, firework);
	}

	@SuppressWarnings("deprecation")
	private void spawnRandomWoolAt(int x, int y, int z, boolean firework)
	{
		Block b = getCenter().getBlock().getRelative(x, y, z);
		Byte color = (byte) UtilMath.r(SCORE_BLOCK_DATA_RANGE);

		while (color == TRAP_BLOCK_DATA)
		{
			color = (byte) UtilMath.r(SCORE_BLOCK_DATA_RANGE);
		}

		setBlock(b, Material.WOOL, color);

		if (firework)
		{
			UtilFirework.playFirework(b.getLocation().add(SCORE_FIREWORK_LOCATION_ADD, SCORE_FIREWORK_LOCATION_ADD, SCORE_FIREWORK_LOCATION_ADD), Type.BALL, DyeColor.getByWoolData(b.getData()).getColor(), false, false);
		}

		_blocks.add(b);
	}

	private void increment(Player player, int amount)
	{
		int score = _score.get(player);
		int updatedScore = score + amount;

		if (updatedScore <= SCORE_GOAL)
		{
			_score.put(player, updatedScore);
		}
	}

	private void subtract(Player player, int amount)
	{
		int score = _score.get(player);
		int updatedScore = score - amount;

		if (updatedScore > 0)
		{
			_score.put(player, updatedScore);
		}
		else
		{
			_score.put(player, 0);
		}
	}

	@Override
	public boolean hasData(Player player)
	{
		return !_stepTracker.contains(player);
	}
}
