package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on speed and colors.
 */
public class ChallengeColorChange extends Challenge
{
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int SPAWN_COORDINATES_MULTIPLE = 2;
	private static final int MAP_HEIGHT = 1;

	private static final long TIME_DELAY = 5000;
	private static final int RESET_DELAY = 3750;
	private static final int TIME_DELAY_DECREMENT_RATE = 600;
	private static final int TIME_DELAY_MIN = 1500;

	private static final int PLATFORM_MULTIPLIER = 2;
	private static final int PLATFORM_SHIFT = 2;
	private static final float FALL_SOUND_VOLUME = 2.0F;
	private static final float FALL_SOUND_PITCH = 0.0F;
	private static final int COUNTDOWN_SOUND_DELAY = 1000;

	private long _modifiedTimeDelay;
	private List<Integer> _colors = new ArrayList<>();
	private int _currentColor;
	private boolean _isFalling;
	private long _lastSound;
	private long _stageDelay;
	private List<Entry<Integer, Integer>> _lastGeneratedPlatforms = new ArrayList<>();

	public ChallengeColorChange(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Color Change",
			"Stand on the correct color.");

		Settings.setUseMapHeight();

		populateColors();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -size; x <= 7; x++)
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
		createPlatformCoordinates();
		createPlatforms();
	}

	@Override
	public void onStart()
	{
		_modifiedTimeDelay = TIME_DELAY;
		_stageDelay = System.currentTimeMillis() + _modifiedTimeDelay;
		_currentColor = UtilMath.randomElement(_colors);

		for (Player player : Host.GetPlayers(false))
		{
			PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 1, true, false);
			player.addPotionEffect(nightVision);
		}

		fillItem(new ItemStack(Material.STAINED_CLAY, 1, (short) _currentColor));
	}

	@Override
	public void onEnd()
	{
		_isFalling = false;
		_lastSound = 0;
		_lastGeneratedPlatforms.clear();

		for (Player player : Host.GetPlayers(false))
		{
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!isChallengeValid())
			return;

		Player[] players = getPlayersAlive().toArray(new Player[0]);

		if (_stageDelay < System.currentTimeMillis())
		{
			UtilTextBottom.displayProgress(0, players);

			if (_isFalling)
			{
				removeAllPlatforms();
				playFallSound();

				_isFalling = false;
				_currentColor = UtilMath.randomElement(_colors);

				createMap();
				addCurrentColorToInventory();

				_modifiedTimeDelay -= TIME_DELAY_DECREMENT_RATE;
				_modifiedTimeDelay = Math.max(_modifiedTimeDelay, TIME_DELAY_MIN);
				_stageDelay = System.currentTimeMillis() + _modifiedTimeDelay;
			}
			else
			{
				_isFalling = true;

				removeDifferentColorPlatforms();

				_stageDelay = System.currentTimeMillis() + RESET_DELAY;
			}
		}
		else if (!_isFalling)
		{
			double amount = (_stageDelay - System.currentTimeMillis()) / (double) _modifiedTimeDelay;
			UtilTextBottom.displayProgress(amount, players);

			playCountdownSound();
		}
		else
		{
			UtilTextBottom.displayProgress(0, players);
		}
	}

	private void populateColors()
	{
		for (int i = 0; i <= 15; i++)
		{
			if (i != 2 && i != 6 && i != 7 && i != 9 && i != 12)
			{
				_colors.add(i);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createPlatformCoordinates()
	{
		List<Entry<Integer, Integer>> platforms = new ArrayList<>();
		int size = getArenaSize() - PLATFORM_SHIFT;

		for (int x = -size; x <= 4; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				platforms.add(new HashMap.SimpleEntry(x * PLATFORM_MULTIPLIER, z * PLATFORM_MULTIPLIER));
			}
		}

		Collections.shuffle(platforms);
		_lastGeneratedPlatforms = platforms;
	}

	private void createPlatforms()
	{
		int i = UtilMath.randomElement(_colors);

		for (Entry<Integer, Integer> platform : _lastGeneratedPlatforms)
		{
			if (i >= _colors.size())
			{
				i = 0;
			}

			byte color = (byte) (int) _colors.get(i);

			i++;

			createPlatformPart(platform.getKey(), platform.getValue(), color);
		}
	}

	private void createPlatformPart(int platformX, int platformZ, byte color)
	{
		for (int x = 0; x <= 1; x++)
		{
			for (int z = 0; z <= 1; z++)
			{
				Block block = getCenter().getBlock().getRelative(platformX + x, 0, platformZ + z);
				setBlockReallyQuicklyAndDangerously(block, Material.STAINED_CLAY, color);
				addBlock(block);
			}
		}
	}

	private void playFallSound()
	{
		for (Player player : getPlayersAlive())
		{
			player.playSound(player.getLocation(), Sound.NOTE_PIANO, FALL_SOUND_VOLUME, FALL_SOUND_PITCH);
		}
	}

	private void addCurrentColorToInventory()
	{
		fillItem(new ItemStack(Material.STAINED_CLAY, 1, (short) _currentColor));
	}

	@SuppressWarnings("deprecation")
	private void removeDifferentColorPlatforms()
	{
		for (Entry<Integer, Integer> platform : _lastGeneratedPlatforms)
		{
			for (int x = 0; x <= 1; x++)
			{
				for (int z = 0; z <= 1; z++)
				{
					Block block = getCenter().getBlock().getRelative(platform.getKey() + x, 0, platform.getValue() + z);

					if (block.getData() != _currentColor)
					{
						setBlockReallyQuicklyAndDangerously(block, Material.AIR, (byte) 0);
					}
				}
			}

		}
	}

	@SuppressWarnings("deprecation")
	private void removeAllPlatforms()
	{
		for (Entry<Integer, Integer> platform : _lastGeneratedPlatforms)
		{
			for (int x = 0; x <= 1; x++)
			{
				for (int z = 0; z <= 1; z++)
				{
					Block block = getCenter().getBlock().getRelative(platform.getKey() + x, 0, platform.getValue() + z);

					if (block.getType() != Material.AIR)
					{
						setBlockReallyQuicklyAndDangerously(block, Material.AIR, (byte) 0);
					}
				}
			}

		}
	}

	private void playCountdownSound()
	{
		if (_lastSound < System.currentTimeMillis())
		{
			_lastSound = System.currentTimeMillis() + COUNTDOWN_SOUND_DELAY;

			for (Player player : getPlayersAlive())
			{
				player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0F, 1.0F);
			}
		}
	}
}
