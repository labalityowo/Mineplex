package nautilus.game.arcade.game.games.smash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.smash.events.SmashActivateEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.modules.TrainingGameModule;
import nautilus.game.arcade.kit.Kit;

public class SuperSmashTraining extends SuperSmash
{

	private static final long GAME_TIME = TimeUnit.HOURS.toMillis(3);
	private static final long GAME_WARN_TIME = GAME_TIME - TimeUnit.MINUTES.toMillis(5);
	private static final String[] INFO_HOLOGRAM = {
			C.cYellow + "Select a " + C.cGreen + "Kit",
			C.cYellow + "Jump off the island to use your abilities",
			C.cYellow + "You can then " + C.cGreen + "PVP" + C.cYellow + " other players",
			C.cYellow + "Click the " + C.cGreen + "Bed" + C.cYellow + " to return to this island"
	};

	private final TrainingGameModule _trainingModule;
	private Location _borderA;
	private Location _borderB;
	private Predicate<Player> _safeFunction = player -> !UtilAlg.inBoundingBox(player.getLocation(), _borderA, _borderB);
	private Map<UUID, Long> _lastDeath;

	private boolean _announceEnd;

	public SuperSmashTraining(ArcadeManager manager)
	{
		super(manager, GameType.SmashTraining, new String[]{
				"Super Smash Mobs Training Ground"
		});

		DamageTeamSelf = true;
		DeathSpectateSecs = 0;
		PrepareTime = 500;
		GiveClock = false;
		HungerSet = 20;

		_lastDeath = new HashMap<>();

		_trainingModule = new TrainingGameModule()
				.setSkillFunction(_safeFunction)
				.setDamageFunction(_safeFunction)
				.setKitSelectFunction(player ->
				{
					Kit kit = GetKit(player);
					return kit != null && kit instanceof SmashKit && !((SmashKit) kit).isSmashActive(player);
				});
		_trainingModule.register(this);
	}

	@Override
	public void ParseData()
	{
		List<Location> locations = WorldData.GetDataLocs("BROWN");

		_borderA = locations.get(0);
		_borderB = locations.get(1);
	}

	@EventHandler
	public void customTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		GameTeam players = GetTeamList().get(0);
		players.SetColor(ChatColor.YELLOW);
		players.SetName("Players");
		players.setDisplayName(C.cYellowB + "Players");
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		List<Location> locations = WorldData.GetDataLocs("BLUE");

		for (Location location : locations)
		{
			spawnInfoHologram(location);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Scoreboard.writeNewLine();

		List<Player> alive = GetPlayers(true);

		Scoreboard.write(C.cYellowB + "Players");

		if (alive.size() > 9)
		{
			Scoreboard.write(alive.size() + " Alive");
		}
		else
		{
			for (Player player : alive)
			{
				Scoreboard.write(player.getName());
			}
		}

		Scoreboard.writeNewLine();

		Scoreboard.write(C.cYellowB + "Time");
		Scoreboard.write(UtilTime.MakeStr(System.currentTimeMillis() - GetStateTime()));

		Scoreboard.writeNewLine();

		Scoreboard.draw();
	}

	@Override
	public List<Player> getWinners()
	{
		return null;
	}

	@Override
	public List<Player> getLosers()
	{
		return null;
	}

	@Override
	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_lastDeath.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void cleanlastDeath(UpdateEvent event)
	{
		if (!IsLive() || event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (_safeFunction.test(player))
			{
				_lastDeath.remove(player.getUniqueId());
			}
		}
	}

	@EventHandler
	public void smashActivate(SmashActivateEvent event)
	{
		if (!_safeFunction.test(event.getPlayer()))
		{
			event.setCancelled(true);
		}

		_trainingModule.preventReturnToSpawn(event.getPlayer());
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		if (UtilTime.elapsed(GetStateTime(), GAME_TIME))
		{
			SetState(GameState.Dead);
			Announce(C.cRedB + "Game Over! Resetting the map, you will be able to play again.");
		}
		else if (UtilTime.elapsed(GetStateTime(), GAME_WARN_TIME) && !_announceEnd)
		{
			_announceEnd = true;
			Announce(C.cRedB + "The Game Will End In 5 Minutes.");
		}
	}

	@Override
	public long getNewSmashTime()
	{
		return (long) (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2) + TimeUnit.MINUTES.toMillis(2) * Math.random());
	}

	@Override
	protected boolean displayKitInfo(Player player)
	{
		if (_lastDeath.containsKey(player.getUniqueId()))
		{
			return UtilTime.elapsed(_lastDeath.get(player.getUniqueId()), 4000);
		}

		return super.displayKitInfo(player) || !_safeFunction.test(player);
	}

	private void spawnInfoHologram(Location location)
	{
		CreatureAllowOverride = true;

		new Hologram(getArcadeManager().getHologramManager(), location, true, INFO_HOLOGRAM).start();

		CreatureAllowOverride = false;
	}
}
