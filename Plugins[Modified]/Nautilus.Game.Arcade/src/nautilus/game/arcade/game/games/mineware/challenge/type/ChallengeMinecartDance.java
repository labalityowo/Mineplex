package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;

/**
 * A challenge based on speed and minecarts.
 */
public class ChallengeMinecartDance extends Challenge
{
	private static final int MAP_SIZE_MIN = 7;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;
	private static final int MAP_HEIGHT = 1;

	private static final int WOOL_DATA_RANGE = 16;
	private static final int RED_FIREWORKS_DELAY = 5500;
	private static final int GREEN_FIREWORKS_AMOUNT = 2;

	private static final int MUSIC_TRACK_DATA = 2259;
	private static final int WORLD_EVENT = 1005;

	private static final int MINECART_DIVIDER = 2;
	private static final int MINECART_MULTIPLIER = 2;
	private static final int MINECART_SPAWN_HEIGHT = 2;
	private static final int PLAYER_AMOUNT_MODIFY_TIMER = 10;
	private static final int TIMER_DIVIDER = 2;
	private static final int TIMER_MIN = 5;

	private static final int MINECARTS_LEFT_FADE_IN_TICKS = 0;
	private static final int MINECARTS_LEFT_STAY_TICKS = 60;
	private static final int MINECARTS_LEFT_FADE_OUT_TICKS = 20;

	private static final int FIREWORK_SHIFT_X = 5;
	private static final int FIREWORK_SPAWN_Y = 10;
	private static final int FIREWORK_SHIFT_Z = 5;

	private enum MinecartDancePhase
	{
		WAITING, STARTED, ENDED
	}

	private MinecartDancePhase _phase;
	private long _timeSinceLastPhase;
	private boolean _isPlayingMusic;
	private int _availableMinecarts;
	private HashSet<Minecart> _minecarts = new HashSet<Minecart>();

	public ChallengeMinecartDance(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Musical Minecart",
			"Get ready when the fireworks change color.",
			"Once they're green, get inside a minecart!");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -(getArenaSize(MAP_SIZE_MIN) - MAP_SPAWN_SHIFT); x <= getArenaSize(MAP_SIZE_MIN); x++)
		{
			for (int z = -(getArenaSize(MAP_SIZE_MIN) - MAP_SPAWN_SHIFT); z <= getArenaSize(MAP_SIZE_MIN); z++)
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
		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						setBlock(block, Material.WOOL, (byte) UtilMath.r(WOOL_DATA_RANGE));
					}
					else
					{
						if (Math.abs(x) == getArenaSize() || Math.abs(z) == getArenaSize())
						{
							setBlock(block, Material.FENCE);
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
		_phase = MinecartDancePhase.WAITING;
		Host.Manager.GetExplosion().SetLiquidDamage(false);
		_timeSinceLastPhase = System.currentTimeMillis();
	}

	@Override
	public void onEnd()
	{
		Host.Manager.GetExplosion().SetLiquidDamage(true);

		if (_isPlayingMusic)
		{
			stopPlayingMusic();
		}

		for (Minecart minecarts : _minecarts)
		{
			minecarts.remove();
		}

		_timeSinceLastPhase = 0;
		_availableMinecarts = 0;
		_minecarts.clear();
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isChallengeValid())
			return;

		if (_phase == MinecartDancePhase.WAITING && event.getType() == UpdateType.SEC)
		{
			if (!_isPlayingMusic)
			{
				startPlayingMusic();
			}

			spawnRedFireworks();
		}
		else if (_phase == MinecartDancePhase.STARTED)
		{
			spawnGreenFireworks();

			if (_isPlayingMusic)
			{
				stopPlayingMusic();
			}

			spawnMinecarts();

			_phase = MinecartDancePhase.ENDED;

			startTimerWhenReady();
		}
		else if (_phase == MinecartDancePhase.ENDED)
		{
			showMinecartsLeft();
		}
	}

	@EventHandler
	public void onInteractionUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		if (!isChallengeValid())
			return;

		if (_phase == MinecartDancePhase.ENDED)
		{
			_availableMinecarts = getAvailableMinecarts();

			if (_availableMinecarts == 0)
			{
				playersLooseIfOutsideMinecart();

				_timeSinceLastPhase = System.currentTimeMillis();
				_phase = MinecartDancePhase.WAITING;

				removeAndClearMinecarts();
			}
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getVehicle() instanceof Minecart)
			event.setCancelled(true);
	}

	@EventHandler
	public void onVehicleInteract(VehicleEnterEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!(event.getEntered() instanceof Player))
			return;

		Player player = (Player) event.getEntered();

		if (Data.isLost(player) || !Host.IsPlaying(player))
		{
			event.setCancelled(true);
		}
	}

	private int getAvailableMinecarts()
	{
		int count = 0;

		for (Minecart minecart : _minecarts)
		{
			if (!minecart.isEmpty())
			{
				continue;
			}
			else
			{
				count++;
			}
		}

		return count;
	}

	private void playersLooseIfOutsideMinecart()
	{
		for (Player player : getPlayersIn(true))
		{
			if (!player.isInsideVehicle())
			{
				setLost(player);
				Host.WorldData.World.strikeLightningEffect(player.getLocation());
			}
		}
	}

	private void removeAndClearMinecarts()
	{
		for (Minecart minecart : _minecarts)
		{
			if (!minecart.isEmpty())
			{
				minecart.eject();
			}

			minecart.remove();
		}

		_minecarts.clear();
	}

	private void startPlayingMusic()
	{
		_isPlayingMusic = true;

		PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(WORLD_EVENT, new BlockPosition(getCenter().getBlockX(), getCenter().getBlockY() + 5, getCenter().getBlockZ()), MUSIC_TRACK_DATA, false);

		for (Player player : UtilServer.getPlayers())
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	private void spawnRedFireworks()
	{
		if (_timeSinceLastPhase + RED_FIREWORKS_DELAY < System.currentTimeMillis())
		{
			if (UtilMath.r(4) == 0)
			{
				_phase = MinecartDancePhase.STARTED;
			}
			else
			{
				spawnFireworkAtCorners(Color.RED);
			}
		}
		else
		{
			spawnFireworkAtCorners(Color.RED);
		}
	}

	private void spawnGreenFireworks()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i <= GREEN_FIREWORKS_AMOUNT; i++)
				{
					spawnFireworkAtCorners(Color.GREEN);
				}
			}
		}.runTaskLater(Host.Manager.getPlugin(), 0);
	}

	private void stopPlayingMusic()
	{
		_isPlayingMusic = false;

		PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(WORLD_EVENT, new BlockPosition(getCenter().getBlockX(), getCenter().getBlockY() + 5, getCenter().getBlockZ()), 0, false);

		for (Player player : UtilServer.getPlayers())
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	private void spawnMinecarts()
	{
		for (int i = 0; i < Math.round(getPlayersIn(true).size() / MINECART_DIVIDER); i++)
		{
			Minecart minecart = Host.WorldData.World.spawn(getCenter().add(UtilMath.r(getArenaSize(MAP_SIZE_MIN) * MINECART_MULTIPLIER) - (getArenaSize(MAP_SIZE_MIN)), MINECART_SPAWN_HEIGHT, UtilMath.r(getArenaSize(MAP_SIZE_MIN) * MINECART_MULTIPLIER) - (getArenaSize(MAP_SIZE_MIN))),
				Minecart.class);
			UtilEnt.ghost(minecart, true, false);
			_minecarts.add(minecart);
		}
	}

	private void startTimerWhenReady()
	{
		if (getPlayersIn(true).size() >= PLAYER_AMOUNT_MODIFY_TIMER)
		{
			startTimer(Math.round(getPlayersIn(true).size() / TIMER_DIVIDER));
		}
		else
		{
			startTimer(TIMER_MIN);
		}
	}

	private void showMinecartsLeft()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (_phase != MinecartDancePhase.ENDED || !isChallengeValid())
				{
					this.cancel();
				}

				int minecarts = 0;

				for (Minecart minecart : _minecarts)
				{
					if (!minecart.isEmpty())
					{
						minecarts++;
					}
				}

				UtilTextMiddle.display(C.cRed + (_minecarts.size() - minecarts), "Minecarts Left!", MINECARTS_LEFT_FADE_IN_TICKS, MINECARTS_LEFT_STAY_TICKS, MINECARTS_LEFT_FADE_OUT_TICKS);
			}
		}.runTaskLater(Host.Manager.getPlugin(), 0);
	}

	private void startTimer(long ticks)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (_phase == MinecartDancePhase.ENDED)
				{
					playersLooseIfOutsideMinecart();

					_timeSinceLastPhase = System.currentTimeMillis();
					_phase = MinecartDancePhase.WAITING;

					removeAndClearMinecarts();
				}
				else
				{
					this.cancel();
				}

			}
		}.runTaskLater(Host.Manager.getPlugin(), ticks * TICK_MULTIPLIER);
	}

	private void spawnFireworkAtCorners(Color color)
	{
		UtilFirework.playFirework(getCenter().add(getArenaSize() + FIREWORK_SHIFT_X, FIREWORK_SPAWN_Y, getArenaSize() + FIREWORK_SHIFT_Z), Type.BALL_LARGE, color, false, false);
		UtilFirework.playFirework(getCenter().add(-getArenaSize() - FIREWORK_SHIFT_X, FIREWORK_SPAWN_Y, getArenaSize() + FIREWORK_SHIFT_Z), Type.BALL_LARGE, color, false, false);
		UtilFirework.playFirework(getCenter().add(getArenaSize() + FIREWORK_SHIFT_X, FIREWORK_SPAWN_Y, -getArenaSize() - FIREWORK_SHIFT_Z), Type.BALL_LARGE, color, false, false);
		UtilFirework.playFirework(getCenter().add(-getArenaSize() - FIREWORK_SHIFT_X, FIREWORK_SPAWN_Y, -getArenaSize() - FIREWORK_SHIFT_Z), Type.BALL_LARGE, color, false, false);
	}
}
