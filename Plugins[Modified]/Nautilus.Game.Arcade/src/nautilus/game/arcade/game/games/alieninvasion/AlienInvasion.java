package nautilus.game.arcade.game.games.alieninvasion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.award.AlienInvasionTrack;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.alieninvasion.kit.KitPlayer;
import nautilus.game.arcade.game.games.dragonescape.DragonScore;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class AlienInvasion extends SoloGame
{

	private static final String[] DESCRIPTION = {
			"You've been captured by Aliens!",
			"Parkour your way to the end.",
			"Use the Paintball Gun to kill Aliens.",
			"Watch out for UFOs!"
	};
	private static final Comparator<DragonScore> SCORE_SORTER = (o1, o2) ->
	{
		if (o1.Score > o2.Score)
		{
			return 1;
		}
		else if (o1.Score < o2.Score)
		{
			return -1;
		}

		return 0;
	};
	private static final String GAME_COMPLETED_STAT = "Alien Invasion Chest Given";

	private final List<BeamSource> _sources = new ArrayList<>();
	private final List<Location> _targets = new ArrayList<>();
	private final ArrayList<Location> _path = new ArrayList<>();

	private int _lastBeamId;
	private long _lastBeam;
	private long _nextBeam;

	private final List<DragonScore> _score = new ArrayList<>(16);

	private final Set<Alien> _aliens = new HashSet<>();

	public AlienInvasion(ArcadeManager manager)
	{
		super(manager, GameType.AlienInvasion, new Kit[]{new KitPlayer(manager)}, DESCRIPTION);

		WorldTimeSet = 18000;
		DamagePvP = false;
		DamageFall = false;
		HungerSet = 20;

		manager.GetCreature().SetDisableCustomDrops(true);

		new CompassModule()
				.register(this);

		for (World world : Bukkit.getWorlds())
		{
			world.setTime(18000);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		Scoreboard.writeNewLine();

		sortScores();

		Scoreboard.writeGroup(_score.subList(0, Math.min(14, _score.size())), score ->
		{
			ChatColor col = IsAlive(score.Player) ? ChatColor.GREEN : ChatColor.RED;
			return Pair.create(col + score.Player.getName(), (int) score.Score);
		}, true);

		Scoreboard.writeNewLine();

		Scoreboard.draw();
	}

	@Override
	public void ParseData()
	{
		// Setup all Beam Sources
		List<Location> sources = WorldData.GetDataLocs("LIME");

		for (Location location : sources)
		{
			_sources.add(new BeamSource(location));
		}

		Location start = WorldData.GetDataLocs("RED").get(0);
		Location last = start;
		List<Location> targets = WorldData.GetDataLocs("BLACK");
		ArrayList<Location> path = new ArrayList<>();
		path.addAll(targets);
		path.addAll(WorldData.GetDataLocs("BROWN"));

		while (!path.isEmpty())
		{
			Location closestPath = UtilAlg.findClosest(last, path);

			if (targets.contains(closestPath))
			{
				_targets.add(closestPath);
			}

			_path.add(closestPath);
			path.remove(closestPath);
			last = closestPath;
		}

		int id = 0;

		for (Location target : _targets)
		{
			BeamSource source = getClosestSource(target);

			source.addBeam(Manager, id++, target);
		}
	}

	private BeamSource getClosestSource(Location location)
	{
		BeamSource best = null;
		double bestDist = Double.MAX_VALUE;

		for (BeamSource source : _sources)
		{
			double dist = UtilMath.offsetSquared(source.getSource(), location);

			if (best == null || dist < bestDist)
			{
				best = source;
				bestDist = dist;
			}
		}

		return best;
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			_score.add(new DragonScore(player, 0));
		}

		for (Team team : Scoreboard.getHandle().getTeams())
		{
			team.setCanSeeFriendlyInvisibles(true);
		}
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_lastBeam = System.currentTimeMillis();
		_nextBeam = 10000;

		for (Player player : GetPlayers(true))
		{
			player.sendMessage(F.main("Game", C.cYellow + "Double Tab Space to use your double jump!"));
		}

		ItemStack glass = new ItemStack(Material.GLASS);

		CreatureAllowOverride = true;
		for (Location location : WorldData.GetDataLocs("BLUE"))
		{
			Skeleton skeleton = UtilVariant.spawnWitherSkeleton(location);

			skeleton.setMaxHealth(4);
			skeleton.getEquipment().setHelmet(glass);
		}

		for (Location location : WorldData.GetDataLocs("LIGHT_BLUE"))
		{
			_aliens.add(new Alien(Manager, location));
		}
		CreatureAllowOverride = false;
	}

	@EventHandler
	public void invisibility(PlayerKitGiveEvent event)
	{
		Manager.GetCondition().Factory().Invisible(GetName(), event.getPlayer(), event.getPlayer(), 40, 0, false, false, false);
	}

	@EventHandler
	public void updateAliens(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		for (Alien alien : _aliens)
		{
			alien.update();
		}

		_aliens.removeIf(alien -> !alien.isValid());
	}

	@EventHandler
	public void updateBeam(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !IsLive())
		{
			return;
		}

		Beam beam = getSuitableBeam();

		if (beam == null)
		{
			return;
		}

		_lastBeam = System.currentTimeMillis();
		_nextBeam -= 100;
		_lastBeamId++;

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (beam.update())
				{
					for (Entry<Player, Double> entry : UtilPlayer.getInRadius(beam.getLastLocation(), 20).entrySet())
					{
						Manager.GetDamage().NewDamageEvent(entry.getKey(), null, null, DamageCause.CUSTOM, 20 * entry.getValue(), false, true, false, GetName(), "Photon Torpedo");
					}

					int killIfBefore = 0;

					for (int i = 0; i < _path.size(); i++)
					{
						if (UtilMath.offsetSquared(beam.getLastLocation(), _path.get(i++)) < 25)
						{
							killIfBefore = i;
							break;
						}
					}

					for (DragonScore score : _score)
					{
						if (score.Score <= killIfBefore)
						{
							Manager.GetDamage().NewDamageEvent(score.Player, null, null, DamageCause.CUSTOM, 9999, false, true, false, GetName(), "Photon Torpedo");
						}
					}

					cancel();
				}
			}
		}, 0, 2);
	}

	private Beam getSuitableBeam()
	{
		if (!UtilTime.elapsed(_lastBeam, _nextBeam))
		{
			return null;
		}

		for (BeamSource beamSource : _sources)
		{
			Beam beam = beamSource.getFromId(_lastBeamId);

			if (beam != null)
			{
				return beam;
			}
		}

		return null;
	}

	@EventHandler
	public void updatePlayerTracker(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			DragonScore score = getScore(player);
			double currentScore = score.Score;
			int newScore = 0;
			Location location = UtilAlg.findClosest(player.getLocation(), _path);

			if (UtilMath.offsetSquared(player.getLocation(), location) > 100)
			{
				break;
			}

			for (int i = 0; i < _path.size(); i++)
			{
				Location a = _path.get(i);

				if (location.equals(a))
				{
					newScore = i;
				}
			}

			if (newScore > currentScore)
			{
				score.Score = newScore;

				// Reward title
				if (score.Score == _path.size() - 1)
				{
					if (Manager.GetStatsManager().Get(player).getStat(GAME_COMPLETED_STAT) == 0)
					{
						Manager.GetStatsManager().incrementStat(player, GAME_COMPLETED_STAT, 1);

						Manager.getInventoryManager().addItemToInventory(success ->
						{
							if (success)
							{
								player.sendMessage(F.main("Game", "Unlocked 1 " + C.cAqua + "Omega Chest" + C.mBody + "."));
							}
							else
							{
								player.sendMessage(F.main("Game", "Failed to give you your Omega Chest, you should inform a staff member!"));
							}

						}, player, "Omega Chest", 1);
					}

					TrackManager trackManager = Manager.getTrackManager();
					Track track = trackManager.getTrack(AlienInvasionTrack.class);

					if (trackManager.hasTrack(player, track))
					{
						continue;
					}

					trackManager.unlockTrack(player, track, result ->
					{
						switch (result)
						{
							case UNKNOWN_ERROR:
								player.sendMessage(F.main("Game", "Oops, somehow I could not give you the title track, you should inform a staff member!"));
								break;
							case SUCCESS:
								player.sendMessage(F.main("Game", "Unlocked " + track.getColor() + track.getLongName() + C.mBody + " Title!"));
								break;
						}
					});
				}
			}
		}
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void entitySpawn(EntitySpawnEvent event)
	{
		if (event.getEntity() instanceof ExperienceOrb)
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		List<Player> alive = GetPlayers(true);

		for (DragonScore score : _score)
		{
			if (score.Score == _path.size() - 1 || alive.isEmpty())
			{
				sortScores();
				List<Player> players = new ArrayList<>(_score.size());

				for (DragonScore score1 : _score)
				{
					AddGems(score1.Player, score1.Score, "Map Progress", false, false);
					players.add(score1.Player);
				}

				Collections.reverse(players);
				AnnounceEnd(players);
				SetState(GameState.End);
				return;
			}
		}
	}

	private void sortScores()
	{
		_score.sort(SCORE_SORTER);
	}

	private DragonScore getScore(Player player)
	{
		for (DragonScore score : _score)
		{
			if (score.Player.equals(player))
			{
				return score;
			}
		}

		return null;
	}

	@Override
	public Location GetSpectatorLocation()
	{
		if (SpectatorSpawn == null)
		{
			return new Location(WorldData.World, 0, 158, 0);
		}

		return SpectatorSpawn;
	}
}
