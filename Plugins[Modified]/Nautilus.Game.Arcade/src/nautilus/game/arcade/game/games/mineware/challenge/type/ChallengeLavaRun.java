package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import mineplex.core.disguise.disguises.DisguiseVillager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.disguises.DisguiseMagmaCube;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on running.
 */
public class ChallengeLavaRun extends Challenge
{
	private static final int CHALLENGE_PLAYERS_MAX = 30;
	private static final int MAP_HEIGHT = 4;
	private static final int MAP_SPAWN_HEIGHT = MAP_HEIGHT + 1;
	private static final int MAP_SPAWN_SHIFT = 3;

	private static final int DELAY_START = 2000; // milliseconds
	private static final int DELAY_MIN = 1000; // milliseconds
	private static final int DISSAPEARING_BLOCKS = 10;

	private static final int OBSIDIAN_LARGE_DISTANCE = 4; // blocks
	private static final int DELAY_BOOST = 300; // milliseconds
	private static final int DELAY_SUBTRACT = 200; // milliseconds
	private static final int DELAY_AFTER_DESTURCTION = 1500; // milliseconds

	private static final float DESTRUCTION_SOUND_VOLUME = 2.0F;
	private static final float DESTRUCTION_SOUND_PITCH = 1.0F;
	private static final double DISTANCE_XZ_ADD = 0.5;

	private int _arenaStartSize;
	private Block _obsidian;
	private Location _lastObsidianLocation;
	private boolean _shouldMoveObsidian;
	private List<Block> _platform;
	private long _modifiedDelay;
	private long _modifiedDelayMin;
	private int _disappearingBlocks;

	public ChallengeLavaRun(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Lava Run",
			"The lava is coming!",
			"Stand on the obsidian.",
			"Run! Run! Run!");

		Settings.setUseMapHeight();
		Settings.setMaxPlayers(CHALLENGE_PLAYERS_MAX);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		_arenaStartSize = getArenaSize();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				spawns.add(getCenter().add(x, MAP_SPAWN_HEIGHT, z));
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		generatePlatform();
	}

	@Override
	public void onStart()
	{
		_obsidian = createObsidianBlock();
		_modifiedDelay = System.currentTimeMillis() + DELAY_START;
		_modifiedDelayMin = DELAY_MIN;
		_disappearingBlocks = DISSAPEARING_BLOCKS;
		createLava();
		disguisePlayers();
		for (Player player : Host.GetPlayers(false))
		{
			PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 1, true, false);
			player.addPotionEffect(nightVision);
		}
	}

	@Override
	public void onEnd()
	{
		_obsidian = null;
		_lastObsidianLocation = null;
		_shouldMoveObsidian = false;
		_platform.clear();
		_modifiedDelay = 0;
		_modifiedDelayMin = 0;
		_disappearingBlocks = 0;

		for (Player player : Host.GetPlayers(false))
		{
			if (Host.getArcadeManager().GetDisguise().getActiveDisguise(player) instanceof DisguiseMagmaCube)
			{
				Host.Manager.GetDisguise().undisguise(player);
			}
		}

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

		if (_modifiedDelay > System.currentTimeMillis())
			return;

		if (_shouldMoveObsidian)
		{
			setBlockReallyQuicklyAndDangerously(_obsidian, Material.AIR, (byte) 0);
			generatePlatform();
			_obsidian = createObsidianBlock();
			blockBreakEffect(_obsidian, false);
			playSound();

			_modifiedDelay = System.currentTimeMillis();

			if (UtilMath.offset2d(_obsidian.getLocation(), _lastObsidianLocation) > OBSIDIAN_LARGE_DISTANCE) // Add 1 second if the obsidian is too far.
			{
				_modifiedDelay += DELAY_BOOST;
			}

			if (_modifiedDelayMin > 0)
			{
				_modifiedDelayMin -= DELAY_SUBTRACT;
				_modifiedDelay += _modifiedDelayMin;
			}

			_disappearingBlocks++;
			_shouldMoveObsidian = false;
		}
		else
		{
			if (isPlatformEmpty())
			{
				_modifiedDelay = System.currentTimeMillis() + DELAY_AFTER_DESTURCTION;
				_lastObsidianLocation = _obsidian.getLocation();
				_shouldMoveObsidian = true;
			}
			else
			{
				destroyPlatform();
			}
		}
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event)
	{
		if (!isChallengeValid())
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!isPlayerValid(event.GetDamageePlayer()))
			return;

		if (event.GetDamagerEntity(true) != null)
			return;

		event.AddMod("Ensure Death", null, event.GetDamageePlayer().getHealth(), false);
	}

	private void generatePlatform()
	{
		_platform = new ArrayList<>();

		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				Block block = getCenter().getBlock().getRelative(x, MAP_HEIGHT, z);
				setBlockReallyQuicklyAndDangerously(block, Material.GLASS, (byte) 0);
				_platform.add(block);
				addBlock(block);
			}
		}
	}

	private void createLava()
	{
		int size = getArenaSize() + MAP_SPAWN_SHIFT;

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				for (int y = 0; y < MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);
					setBlockReallyQuicklyAndDangerously(block, Material.STATIONARY_LAVA, (byte) 0);
					addBlock(block);
				}
			}
		}
	}

	private Block createObsidianBlock()
	{
		Block block = getCenter().add(UtilMath.r(_arenaStartSize), MAP_HEIGHT, UtilMath.r(_arenaStartSize)).getBlock();
		setBlockReallyQuicklyAndDangerously(block, Material.OBSIDIAN, (byte) 0);
		return block;
	}

	private void disguisePlayers()
	{
		for (Player player : getPlayersAlive())
		{
			DisguiseMagmaCube disguise = new DisguiseMagmaCube(player);
			disguise.SetSize(1);
			Host.getArcadeManager().GetDisguise().disguise(disguise);
		}
	}

	private void playSound()
	{
		for (Player player : getPlayersAlive())
		{
			player.playSound(player.getLocation(), Sound.NOTE_PIANO, DESTRUCTION_SOUND_VOLUME, DESTRUCTION_SOUND_PITCH);
		}
	}

	private boolean isPlatformEmpty()
	{
		int emptyBlocks = 0;

		for (Block part : _platform)
		{
			if (part.isEmpty())
			{
				emptyBlocks++;
			}
		}

		return emptyBlocks == _platform.size() - 1;
	}

	private void destroyPlatform()
	{
		HashMap<Block, Double> distance = new HashMap<Block, Double>();

		for (Block part : _platform)
		{
			distance.put(part, part.getLocation().add(DISTANCE_XZ_ADD, 0, DISTANCE_XZ_ADD).distance(_obsidian.getLocation()));
		}

		_platform.sort((o1, o2) -> distance.get(o2).compareTo(distance.get(o1)));

		for (int i = 0; i < Math.min(_disappearingBlocks, _platform.size()); i++)
		{
			Block block = _platform.get(0);

			if (!block.equals(_obsidian)) // We do not want to remove the obsidian block.
			{
				_platform.remove(0);
				setBlockReallyQuicklyAndDangerously(block, Material.AIR, (byte) 0);
			}
		}
	}
}
