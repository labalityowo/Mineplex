package mineplex.game.nano.game.games.musicminecart;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.noteblock.LoopedNotePlayer;
import mineplex.core.noteblock.NBSReader;
import mineplex.core.noteblock.NoteSong;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class MusicMinecarts extends SoloGame
{

	private final Set<Minecart> _minecarts;
	private final LoopedNotePlayer _notePlayer;

	private Location _center;
	private List<Location> _floor;
	private double _floorRadius;
	private boolean _spawned;

	public MusicMinecarts(NanoManager manager)
	{
		super(manager, GameType.MUSIC_MINECARTS, new String[]
				{
						"Musical Chairs!",
						"When the music " + C.cRed + "Stops" + C.Reset + " get in a " +  C.cYellow + "Minecart",
						C.cYellow + "Last player" + C.Reset + " standing wins!"
				});

		_minecarts = new HashSet<>();

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent.setPvp(false);
		_damageComponent.setFall(false);

		NoteSong song;

		try
		{
			song = NBSReader.loadSong(".." + File.separator + ".." + File.separator + "update" + File.separator + "songs" + File.separator + "bebop.nbs");
		}
		catch (FileNotFoundException e)
		{
			song = null;
			e.printStackTrace();
		}

		if (song != null)
		{
			_notePlayer = new LoopedNotePlayer(getLifetime(), song);
			getLifetime().register(_notePlayer, Collections.singleton(GameState.Live));
		}
		else
		{
			_notePlayer = null;
		}
	}

	@Override
	protected void parseData()
	{
		_center = _mineplexWorld.getIronLocation("RED");

		Block toSet = _center.getBlock().getRelative(BlockFace.DOWN);
		Material floorType = toSet.getType();
		byte floorData = toSet.getData();

		toSet.setType(Material.AIR);

		_floor = _mineplexWorld.getIronLocations("ORANGE");
		_floor.sort((o1, o2) -> Double.compare(UtilMath.offset2dSquared(_center, o2), UtilMath.offset2dSquared(_center, o1)));
		_floor.forEach(location -> MapUtil.QuickChangeBlockAt(location, floorType, floorData));
		_floorRadius = UtilMath.offset2d(_center, _floor.get(0));
	}

	@Override
	public void disable()
	{
		_minecarts.clear();
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.getPlayersToInform().clear();
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		runRound(true);
	}

	private void runRound(boolean first)
	{
		updateArenaSize();

		int spawnAt = UtilMath.rRange(4, 10) + (first ? 5 : 0);
		int destroyAt = 15 + spawnAt;

		if (_notePlayer != null)
		{
			_notePlayer.setPlaying(true);
		}

		getManager().runSyncTimer(new BukkitRunnable()
		{
			int seconds = 0;

			@Override
			public void run()
			{
				if (++seconds == spawnAt)
				{
					_spawned = true;

					if (_notePlayer != null)
					{
						_notePlayer.setPlaying(false);
					}

					UtilTextMiddle.display(null, "Get in a " + C.cRed + "Minecart", 0, 20, 10, getAlivePlayers().toArray(new Player[0]));

					int toSpawn = (int) Math.ceil(getAlivePlayers().size() / 2D);
					FireworkEffect effect = FireworkEffect.builder()
							.with(Type.BALL)
							.withColor(Color.RED)
							.build();

					for (int i = 0; i < toSpawn; i++)
					{
						Location location = UtilAlg.Random(_floor);

						if (location == null)
						{
							return;
						}

						location = location.clone().add(0, 8, 0);

						_minecarts.add(location.getWorld().spawn(location, Minecart.class));

						if (i < 5)
						{
							UtilFirework.playFirework(location, effect);
						}
					}
				}

				if (!_spawned)
				{
					return;
				}

				boolean end = true;

				for (Minecart minecart : _minecarts)
				{
					if (minecart.getPassenger() == null)
					{
						end = false;
					}
				}

				if (end || seconds == destroyAt)
				{
					_spawned = false;

					for (Player player : getAlivePlayers())
					{
						if (player.getVehicle() != null)
						{
							player.leaveVehicle();
							continue;
						}

						getManager().getDamageManager().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, getGameType().getName(), "Too Slow");
					}

					_minecarts.forEach(Entity::remove);
					_minecarts.clear();
					cancel();

					if (isLive())
					{
						runRound(false);
					}
				}
			}
		}, 0, 20);
	}

	private void updateArenaSize()
	{
		double radius = Math.max(getAlivePlayers().size() * 4, 10);

		if (_floorRadius > radius)
		{
			announce(F.main(getManager().getName(), "Watch out! The arena is getting smaller!"));

			getManager().runSyncTimer(new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!isLive() || _floorRadius <= radius)
					{
						cancel();
						return;
					}

					Iterator<Location> iterator = _floor.iterator();

					while (iterator.hasNext())
					{
						Location location = iterator.next();

						if (UtilMath.offset2d(_center, location) <= _floorRadius)
						{
							break;
						}

						MapUtil.QuickChangeBlockAt(location, Material.AIR);
						iterator.remove();
					}

					_floorRadius--;

					for (Player player : _mineplexWorld.getWorld().getPlayers())
					{
						player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 0.5F);
					}
				}
			}, 0, 4);
		}
	}

	@EventHandler
	public void vehicleLeave(VehicleExitEvent event)
	{
		if (_spawned)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void vehicleDamage(VehicleDamageEvent event)
	{
		event.setCancelled(true);
	}
}
