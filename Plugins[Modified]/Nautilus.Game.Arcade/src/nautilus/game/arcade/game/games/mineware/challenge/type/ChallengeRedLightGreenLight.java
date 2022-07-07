package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on racing.
 */
public class ChallengeRedLightGreenLight extends Challenge
{
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 2;
	private static final int MAP_X_START = -36;
	private static final int MAP_X_STOP = 23;
	private static final int MAP_SPAWN_FIXED_X = -35;
	private static final int VILLAGER_X = MAP_X_STOP - 4;

	private static final int MIN_TIME_BEFORE_RED = 1500; // milliseconds
	private static final int MIN_TIME_BEFORE_GREEN = 2500; // milliseconds

	private static final int COOLDOWN_EXPIRE_TICKS = 35;
	private static final int SLOW_EFFECT_AMPLIFIER = 2;
	private static final int COLOR_TILE_SIZE = 6;

	private static final int KNOCKBACK_HEIGHT = MAP_HEIGHT + 3;
	private static final double KNOCKBACK_POWER = 1.5;
	private static final double KNOCKBACK_Y = 0.4;
	private static final int KNOCKBACK_Y_MAX = 10;

	private static final int CANNOT_MOVE_RANDOMIZER = 2;
	private static final int CAN_MOVE_RANDOMIZER = 3;

	private static final float STATUS_SOUND_VOLUME = 2.0F;
	private static final float STATUS_SOUND_PITCH = 1.0F;
	private static final int DELAY_UNTIL_KNOCKBACK = 1500; // milliseconds
	private static final int CUSTOM_TITLE_STAY_TICKS = 60;

	private static final int FIREWORK_MULTIPLIER = 2;
	private static final int FIREWORK_INCREMENTATION = 4;
	private static final int FIREWORK_X = MAP_X_STOP - 1;
	private static final int FIREWORK_Y = MAP_HEIGHT + 8;

	private static final byte[] COLORS = { 0, 5, 4, 1, 6, 14, 11, 12 };

	private long _timeSinceLastRed;
	private long _timeSinceLastGreen;
	private Villager _villager;
	private boolean _canMove;
	private long _timeBeforeAction;
	private List<Player> _cooldown = new ArrayList<>();
	private int _colorIndex;
	private int _colorCounter;

	public ChallengeRedLightGreenLight(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Red Light, Green Light",
			"Be the first to reach the end.",
			"You can move when the fireworks are green.",
			"Stay still when the fireworks turn red.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int z = -size; z <= size; z++)
		{
			spawns.add(getCenter().add(MAP_SPAWN_FIXED_X, MAP_HEIGHT, z));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int x = MAP_X_START; x <= MAP_X_STOP; x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				Block block = getCenter().getBlock().getRelative(x, 1, z);

				if (x == VILLAGER_X)
				{
					setBlock(block, Material.COAL_BLOCK);
				}
				else
				{
					setBlock(block, Material.WOOL, getColor());
				}

				addBlock(block);
			}

			_colorCounter++;
		}
	}

	@Override
	public void onStart()
	{
		spawnVillager();
		changeMoveState(true);

		addEffect(PotionEffectType.SLOW, SLOW_EFFECT_AMPLIFIER);
	}

	@Override
	public void onEnd()
	{
		if (_villager != null)
		{
			_villager.remove();
		}

		_villager = null;
		_canMove = false;
		_timeBeforeAction = 0;
		_cooldown.clear();
		_colorIndex = 0;
		_colorCounter = 0;
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!isChallengeValid())
			return;

		determineMoveState();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (player.getLocation().getX() > getCenter().getX() + VILLAGER_X)
		{
			setCompleted(player);
		}
		else if (!_canMove && !_cooldown.contains(player) && _timeBeforeAction < System.currentTimeMillis())
		{
			if (event.getFrom().getBlockX() == event.getTo().getBlockX())
				return;

			addCooldown(player);
			expireCooldown(player);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (_cooldown.contains(player))
		{
			_cooldown.remove(player);
		}
	}

	private byte getColor()
	{
		if (_colorCounter >= COLOR_TILE_SIZE)
		{
			_colorCounter = 0;
			_colorIndex++;

			if (_colorIndex >= COLORS.length)
				_colorIndex = 0;
		}

		return COLORS[_colorIndex];
	}

	private void spawnVillager()
	{
		Host.CreatureAllow = true;

		Location spawn = getCenter().add(VILLAGER_X, MAP_HEIGHT, 0);
		_villager = (Villager) getCenter().getWorld().spawnEntity(spawn, EntityType.VILLAGER);

		UtilEnt.vegetate(_villager);
		UtilEnt.CreatureLook(_villager, Host.GetSpectatorLocation());
		UtilEnt.ghost(_villager, true, false);

		_villager.setCustomName(C.cGreenB + "Finish Line");
		_villager.setCustomNameVisible(true);

		Host.CreatureAllow = false;
	}

	private void addCooldown(Player player)
	{
		UtilAction.velocity(player, UtilAlg.getTrajectory2d(player.getLocation(), getCenter().add(MAP_X_START, KNOCKBACK_HEIGHT, 0)), KNOCKBACK_POWER, true, KNOCKBACK_Y, 0, KNOCKBACK_Y_MAX, true);
		_cooldown.add(player);
	}

	private void expireCooldown(Player player)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!isChallengeValid() || player == null)
				{
					cancel();
					return;
				}

				_cooldown.remove(player);
			}
		}.runTaskLater(Host.Manager.getPlugin(), COOLDOWN_EXPIRE_TICKS);
	}

	private void determineMoveState()
	{
		if (_canMove)
		{
			toggleCannotMoveState();
		}
		else
		{
			toggleCanMoveState();
		}
	}

	private void toggleCannotMoveState()
	{
		if (_timeSinceLastRed + MIN_TIME_BEFORE_RED < System.currentTimeMillis())
		{
			if (UtilMath.r(CANNOT_MOVE_RANDOMIZER) == 0)
			{
				changeMoveState(false);
			}
		}
	}

	private void toggleCanMoveState()
	{
		if (_timeSinceLastGreen + MIN_TIME_BEFORE_GREEN < System.currentTimeMillis())
		{
			if (UtilMath.r(CAN_MOVE_RANDOMIZER) == 0)
			{
				changeMoveState(true);
			}
		}
	}

	private void changeMoveState(boolean flag)
	{
		if (flag != _canMove)
		{
			if (flag)
			{
				canMoveEffect();
			}
			else
			{
				cannotMoveEffect();
			}
		}
	}

	private void canMoveEffect()
	{
		_timeSinceLastRed = System.currentTimeMillis();
		_canMove = true;
		spawnFirework(Color.GREEN);

		moveTextAndSound();
	}

	private void moveTextAndSound()
	{
		for (Player player : getPlayersIn(true))
		{
			UtilPlayer.message(player, F.main("Green Light", "You can now move."));
			player.getWorld().playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, STATUS_SOUND_VOLUME, STATUS_SOUND_PITCH);
		}
	}

	private void cannotMoveEffect()
	{
		_timeSinceLastGreen = System.currentTimeMillis();
		_timeBeforeAction = System.currentTimeMillis() + DELAY_UNTIL_KNOCKBACK;
		_canMove = false;
		spawnFirework(Color.RED);

		cannotMoveTextAndSound();
	}

	private void cannotMoveTextAndSound()
	{
		for (Player player : getPlayersIn(true))
		{
			UtilPlayer.message(player, F.main("Red Light", "Freeze!"));
			alert(player, ChatColor.RED + "Freeze!", CUSTOM_TITLE_STAY_TICKS);
			player.getWorld().playSound(player.getLocation(), Sound.NOTE_BASS, STATUS_SOUND_VOLUME, STATUS_SOUND_PITCH);
		}
	}

	private void spawnFirework(Color color)
	{
		for (int i = -getArenaSize(); i < getArenaSize() * FIREWORK_MULTIPLIER; i += FIREWORK_INCREMENTATION)
		{
			UtilFirework.playFirework(getCenter().add(FIREWORK_X, FIREWORK_Y, i), Type.BALL_LARGE, color, false, false);
		}
	}
}
