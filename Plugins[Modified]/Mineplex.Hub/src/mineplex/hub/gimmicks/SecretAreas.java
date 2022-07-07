package mineplex.hub.gimmicks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.noteblock.NBSReader;
import mineplex.core.noteblock.NoteSong;
import mineplex.core.noteblock.SingleRunNotePlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;
import mineplex.hub.HubManager;

@ReflectivelyCreateMiniPlugin
public class SecretAreas extends MiniPlugin
{

	private static final String TANK_FILE = ".." + File.separator + ".." + File.separator + "update" + File.separator + "songs" + File.separator + "bebop.nbs";
	private static final long TANK_LENGTH = TimeUnit.MINUTES.toMillis(2);

	private final BlockRestore _restore;

	private final Block _konamiLocation;
	private int _konamiCode;
	private final Block _tankLocation;
	private NoteSong _tankSong;
	private long _lastTank;
	private final Location _cannonBall;
	private final List<Location> _cannons;

	private SecretAreas()
	{
		super("Secret Areas");

		_restore = require(BlockRestore.class);

		MineplexWorld worldData = require(HubManager.class).getWorldData();
		_konamiLocation = worldData.getSpongeLocation("KONAMI").getBlock();
		_tankLocation = worldData.getSpongeLocation("TANK").getBlock().getRelative(BlockFace.DOWN);

		try
		{
			_tankSong = NBSReader.loadSong(TANK_FILE);
		}
		catch (FileNotFoundException e)
		{
		}

		_cannonBall = worldData.getSpongeLocation("CANNON BALL");
		_cannons = worldData.getSpongeLocations("CANNON");
	}

	@EventHandler
	public void secretDoorInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Block clicked = event.getClickedBlock();
		Block upBlock = _konamiLocation.getRelative(BlockFace.EAST);
		boolean up = upBlock.equals(clicked);
		boolean down = upBlock.getRelative(BlockFace.EAST).equals(clicked);
		boolean left = upBlock.getRelative(BlockFace.SOUTH).equals(clicked);
		boolean right = upBlock.getRelative(BlockFace.NORTH).equals(clicked);

		if (!up && !down && !left && !right)
		{
			return;
		}

		if (up)
		{
			if (_konamiCode <= 1)
			{
				_konamiCode++;
			}
			else
			{
				_konamiCode = 0;
			}
		}
		else if (down)
		{
			if (_konamiCode == 2 || _konamiCode == 3)
			{
				_konamiCode++;
			}
			else
			{
				_konamiCode = 0;
			}
		}
		else if (left)
		{
			if (_konamiCode == 4 || _konamiCode == 6)
			{
				_konamiCode++;
			}
			else
			{
				_konamiCode = 0;
			}
		}
		else
		{
			if (_konamiCode == 5)
			{
				_konamiCode++;
			}
			else if (_konamiCode == 7)
			{
				_restore.add(_konamiLocation.getRelative(BlockFace.DOWN), 0, (byte) 0, 4000);
				playZeldaSound(player);
				_konamiCode = 0;
			}
			else
			{
				_konamiCode = 0;
			}
		}
	}

	@EventHandler
	public void tankInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK) || _tankSong == null || !UtilTime.elapsed(_lastTank, TANK_LENGTH))
		{
			return;
		}

		Block clicked = event.getClickedBlock();

		if (clicked.equals(_tankLocation))
		{
			Location location = clicked.getLocation();

			UtilParticle.PlayParticleToAll(ParticleType.NOTE, location, 1, 1, 1, 0, 10, ViewDist.NORMAL);
			new SingleRunNotePlayer(_tankSong, other -> UtilMath.offsetSquared(other.getLocation(), location) < 120).start();
			_lastTank = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void updateCannon(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : UtilPlayer.getNearby(_cannonBall, 3))
		{
			player.teleport(UtilAlg.Random(_cannons));
			runSyncTimer(new BukkitRunnable()
			{
				int iterations = 0;

				@Override
				public void run()
				{
					applyCannonVelocity(player);

					if (++iterations == 3 || !player.isOnline())
					{
						cancel();
					}
				}
			}, 0, 20);
		}
	}

	private void applyCannonVelocity(Player player)
	{
		Location location = player.getLocation();
		player.getWorld().playSound(location, Sound.EXPLODE, 2, 1);
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, 0, 0, 0, 0, 3, ViewDist.NORMAL);
		UtilAction.velocity(player, new Vector(0, 4, 8));
	}

	private void playZeldaSound(Player player)
	{
		player.getWorld().playSound(player.getLocation(), Sound.PISTON_RETRACT, 1, 0.9F);

		runSyncTimer(new BukkitRunnable()
		{
			int note = 0;

			@Override
			public void run()
			{
				float pitch = 0;

				switch (note)
				{
					case 0:
						pitch = 1.059F;
						break;
					case 1:
						pitch = 1;
						break;
					case 2:
						pitch = 0.840F;
						break;
					case 3:
						pitch = 0.594F;
						break;
					case 4:
						pitch = 0.561F;
						break;
					case 5:
						pitch = 0.890F;
						break;
					case 6:
						pitch = 1.12F;
						break;
					case 7:
						pitch = 1.414F;
						break;
				}

				player.getWorld().playSound(player.getLocation(), Sound.NOTE_PIANO, 1, pitch);

				if (++note == 8)
				{
					cancel();
				}
			}
		}, 2, 4);
	}
}
