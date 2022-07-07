package mineplex.game.nano.game.games.parkour;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GamePlacements;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.event.PlayerGameApplyEvent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Parkour extends SoloGame
{

	private final Comparator<ParkourPlace> _sorter = (o1, o2) ->
	{
		boolean o1F = o1.FinishedAt > 0, o2F = o2.FinishedAt > 0;

		if (o1F && o2F)
		{
			return Long.compare(o1.FinishedAt, o2.FinishedAt);
		}
		else if (o1F)
		{
			return -1;
		}
		else if (o2F)
		{
			return 1;
		}

		return Integer.compare(o2.Index, o1.Index);
	};
	private final List<ParkourPlace> _places;
	private final List<Location> _path;
	private final List<Integer> _checkpointIndexes;

	private final long _timeout;
	private boolean _oneFinished;

	public Parkour(NanoManager manager)
	{
		super(manager, GameType.PARKOUR, new String[]
				{
						C.cYellow + "Parkour" + C.Reset + " your way to the " + C.cGreen + "Goal" + C.Reset + ".",
						C.cRed + "Dying" + C.Reset + " returns you back to the " + C.cGreen + "Start" + C.Reset + "!",
						C.cYellow + "First player" + C.Reset + " to the end wins!"
				});

		_places = new ArrayList<>();
		_path = new ArrayList<>();
		_checkpointIndexes = new ArrayList<>();

		_timeout = TimeUnit.MINUTES.toMillis(4);
		_endComponent.setTimeout(_timeout);

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_spectatorComponent.setDeathOut(false);

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			scoreboard.write(C.cYellowB + "Players");

			for (ParkourPlace place : _places.subList(0, Math.min(12, _places.size())))
			{
				Player other = place.Parkourer;
				String colour = "";

				if (other.equals(player))
				{
					colour = C.cGreen;
				}
				else if (place.FinishedAt > 0)
				{
					colour = C.cYellow;
				}

				scoreboard.write(colour + other.getName());
			}

			scoreboard.writeNewLine();

			scoreboard.draw();
		});
	}

	@Override
	protected void parseData()
	{
		List<Location> path = _mineplexWorld.getIronLocations("BLACK");
		List<Location> checkpoints = _mineplexWorld.getIronLocations("YELLOW");
		List<Location> lookAt = _mineplexWorld.getIronLocations("ORANGE");
		Location start = _playersTeam.getSpawn();

		path.addAll(checkpoints);

		while (!path.isEmpty())
		{
			start = UtilAlg.findClosest(start, path);

			if (checkpoints.contains(start))
			{
				_checkpointIndexes.add(_path.size());
				start.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(start, UtilAlg.findClosest(start, lookAt))));
			}

			_path.add(start);
			path.remove(start);
		}
	}

	@Override
	public boolean endGame()
	{
		return false;
	}

	@Override
	public void disable()
	{
		_places.clear();
		_path.clear();
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.getPlayersToInform().clear();
	}

	@EventHandler
	public void playerApply(PlayerGameApplyEvent event)
	{
		if (!isLive())
		{
			return;
		}

		Player player = event.getPlayer();

		for (ParkourPlace parkourPlace : _places)
		{
			if (player.equals(parkourPlace.Parkourer) && parkourPlace.CheckpointIndex >= 0)
			{
				event.setRespawnLocation(_path.get(parkourPlace.CheckpointIndex));
			}
		}
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

		for (ParkourPlace parkourPlace : _places)
		{
			if (parkourPlace.Parkourer.equals(player))
			{
				return;
			}
		}

		_places.add(new ParkourPlace(player));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void sortPlaces(UpdateEvent event)
	{
		if (!isLive())
		{
			return;
		}

		if (event.getType() == UpdateType.FAST)
		{
			_places.sort(_sorter);
		}
		else if (event.getType() == UpdateType.FASTER)
		{
			_places.forEach(parkourPlace ->
			{
				if (parkourPlace.FinishedAt > 0)
				{
					return;
				}

				Player player = parkourPlace.Parkourer;
				Location location = player.getLocation();
				int current = parkourPlace.Index;

				for (int i = 0; i < _path.size(); i++)
				{
					if (!isNearPath(location, i))
					{
						continue;
					}

					if (current + 3 < i && parkourPlace.CheckpointIndex < i)
					{
						getManager().getDamageManager().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, getGameType().getName(), "Cheating");
						return;
					}

					int checkpointIndex = _checkpointIndexes.indexOf(i);

					if (checkpointIndex >= 0 && parkourPlace.CheckpointIndex < i)
					{
						player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
						UtilTextMiddle.display("", C.cYellowB + "Checkpoint!", 0, 30, 20, player);
						parkourPlace.CheckpointIndex = i;
					}
					else if (i == _path.size() - 1)
					{
						addSpectator(player, false, false);
						parkourPlace.FinishedAt = System.currentTimeMillis();

						announce(F.main(getManager().getName(), F.name(player.getName()) + " finished the course in " + F.time(UtilTime.MakeStr(parkourPlace.FinishedAt - getStateTime())) + "!"), null);
						UtilTextMiddle.display(C.cGreen + "End!", "You reached the end of the course!", 0, 50, 20, player);

						if (!_oneFinished)
						{
							_oneFinished = true;

							long elapsed = parkourPlace.FinishedAt - getStateTime();
							long left = _timeout - elapsed;
							long reduceTo = TimeUnit.SECONDS.toMillis(30);

							if (left > reduceTo)
							{
								// Set the game to timeout in 30 seconds
								_endComponent.setTimeout(elapsed + reduceTo);
								announce(F.main(getManager().getName(), "The game will now end in " + F.time(UtilTime.MakeStr(reduceTo)) + "!"));
							}
						}
					}

					parkourPlace.Index = i;
				}
			});
		}
	}

	private boolean isNearPath(Location location, int pathIndex)
	{
		if (pathIndex >= _path.size())
		{
			return false;
		}

		return UtilMath.offsetSquared(location, _path.get(pathIndex)) < 16;
	}

	@Override
	protected GamePlacements createPlacements()
	{
		_playersTeam.getActualPlaces().clear();
		_places.forEach(parkourPlace -> _playersTeam.addPlacementBottom(parkourPlace.Parkourer));

		return GamePlacements.fromTeamPlacements(_playersTeam.getActualPlaces());
	}

	@EventHandler
	public void playerOut(PlayerStateChangeEvent event)
	{
		if (!event.isAlive())
		{
			_places.removeIf(parkourPlace -> parkourPlace.Parkourer.equals(event.getPlayer()));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		event.AddMod(getGameType().getName(), 500);
	}

	private class ParkourPlace
	{

		final Player Parkourer;
		int Index, CheckpointIndex = -1;
		long FinishedAt;

		ParkourPlace(Player parkourer)
		{
			Parkourer = parkourer;
		}
	}
}
