package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextMiddle;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.TeamChallenge;

/**
 * A team based challenge where the side with the fewest players wins.
 */
public class ChallengePickASide extends TeamChallenge
{
	private static final int CHALLENGE_PLAYERS_MIN = 3;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 2;
	private static final int MAP_SPAWN_HEIGHT = MAP_HEIGHT - 1;
	private static final int MAP_X_START = -10;
	private static final int MAP_X_STOP = 10;
	private static final int MAP_SPAWN_X_START = MAP_X_START + 2;
	private static final int MAP_SPAWN_X_STOP = MAP_X_STOP - 2;

	private static final byte BLUE_STAINED_CLAY = 11;
	private static final byte RED_STAINED_CLAY = 14;

	private static final int COUNTER = 5;
	private static final long COUNTDOWN_PREPARE_TICKS = 30L;
	private static final long COUNTDOWN_START_TICKS = 80L;
	private static final long COUNTDOWN_UPDATE_TICKS = 20L;

	private static final float COUNTER_SOUND_VOLUME = 1.0F;
	private static final float COUNTER_SOUND_PITCH = 1.5F;

	private static final int COUNTER_COLOR_1 = 3;
	private static final int COUNTER_COLOR_2 = 2;
	private static final int COUNTER_COLOR_3 = 1;

	private int _counter;

	public ChallengePickASide(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Blue",
			"Red",
			"Pick a Side",
			"Choose one of the two sides.",
			"The side with the fewest players wins.");

		Settings.setMinPlayers(CHALLENGE_PLAYERS_MIN);
		Settings.setUseMapHeight();
		Settings.setTeamBased();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = MAP_SPAWN_X_START; x <= MAP_SPAWN_X_STOP; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				if (x % 2 == 0 && z % 2 == 0)
				{
					spawns.add(getCenter().add(x, MAP_SPAWN_HEIGHT, z));
				}
			}
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
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0 || Math.abs(x) == MAP_X_STOP || Math.abs(z) == getArenaSize())
					{
						if (y > 0 && Math.abs(z) != getArenaSize())
							continue;

						setBlock(block, Material.STAINED_CLAY, (byte) (z < 0 ? BLUE_STAINED_CLAY : RED_STAINED_CLAY));
						addBlock(block);
					}
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		_counter = COUNTER;

		startMessageTask();
		startCountdownTask();
	}

	private void startMessageTask()
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

				for (Player player : getPlayersAlive())
				{
					alert(player, "Prepare to choose your side.");
				}
			}
		}.runTaskLater(Host.Manager.getPlugin(), COUNTDOWN_PREPARE_TICKS);
	}

	private void startCountdownTask()
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

				startCounterTask();
			}
		}.runTaskLater(Host.Manager.getPlugin(), COUNTDOWN_START_TICKS);
	}

	private void startCounterTask()
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

				if (_counter > 0)
				{
					displayCounter();
				}
				else
				{
					cancel();
					return;
				}

				_counter--;
			}
		}.runTaskTimer(Host.Manager.getPlugin(), 0L, COUNTDOWN_UPDATE_TICKS);
	}

	private void displayCounter()
	{
		for (Player player : getPlayersAlive())
		{
			UtilTextMiddle.display(C.Bold + formattedCounter(), null, player);
			player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, COUNTER_SOUND_VOLUME, COUNTER_SOUND_PITCH);
		}
	}

	private String formattedCounter()
	{
		if (_counter == COUNTER_COLOR_1)
			return C.cGreen + _counter;
		else if (_counter == COUNTER_COLOR_2)
			return C.cGold + _counter;
		else if (_counter == COUNTER_COLOR_3)
			return C.cRed + _counter;
		else
			return C.cWhite + _counter;
	}

	@Override
	public boolean canFinish()
	{
		if (_counter <= 0)
		{
			determineSideSize();

			if (getFirstTeam().getSize() < getSecondTeam().getSize())
			{
				selectBlueAsWinners();
			}
			else if (getFirstTeam().getSize() > getSecondTeam().getSize())
			{
				selectRedAsWinners();
			}
			else
			{
				if (UtilMath.random.nextBoolean())
				{
					selectRedAsWinners();
				}
				else
				{
					selectBlueAsWinners();
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	private void determineSideSize()
	{
		for (Player player : getPlayersAlive())
		{
			double z = player.getLocation().getZ();

			if (z < 10)
			{
				getFirstTeam().add(player);
			}
			else
			{
				getSecondTeam().add(player);
			}
		}
	}

	private void selectBlueAsWinners()
	{
		for (Player bluePlayer : getFirstTeam().getPlayers())
		{
			setCompleted(bluePlayer);
		}

		for (Player redPlayer : getSecondTeam().getPlayers())
		{
			setLost(redPlayer);
		}
	}

	private void selectRedAsWinners()
	{
		for (Player redPlayer : getSecondTeam().getPlayers())
		{
			setCompleted(redPlayer);
		}

		for (Player bluePlayer : getFirstTeam().getPlayers())
		{
			setLost(bluePlayer);
		}
	}
}
