package nautilus.game.arcade.game.games.moba.boss;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.boss.wither.WitherBoss;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import nautilus.game.arcade.world.WorldData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BossManager implements Listener
{

	private final Moba _host;

	private final Set<MobaBoss> _bosses;
	private final Map<GameTeam, WitherBoss> _teamBosses;

	private boolean _dummyBosses;

	public BossManager(Moba host)
	{
		_host = host;
		_bosses = new HashSet<>();
		_teamBosses = new HashMap<>(2);
	}

	private void spawnTeamWithers()
	{
		if (_dummyBosses)
		{
			return;
		}

		_host.CreatureAllowOverride = true;

		WorldData worldData = _host.WorldData;

		// Spawn Team Withers
		for (GameTeam team : _host.GetTeamList())
		{
			WitherBoss boss = new WitherBoss(_host, worldData.GetDataLocs(team.GetName().toUpperCase()).get(0), team);
			boss.setup();

			_teamBosses.put(team, boss);
		}

		_host.CreatureAllowOverride = false;
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		spawnTeamWithers();
	}

	@EventHandler
	public void cleanup(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End && event.GetState() != GameState.Dead)
		{
			return;
		}

		_teamBosses.forEach((team, witherBoss) -> witherBoss.cleanup());
		_bosses.forEach(MobaBoss::cleanup);
		_bosses.clear();
	}

	public void registerBoss(MobaBoss boss)
	{
		_bosses.add(boss);
		boss.setup();
	}

	public String getWitherDisplayString(GameTeam team)
	{
		WitherBoss boss = getWitherBoss(team);
		return MobaUtil.getColour(boss.getHealthPercentage()) + "♚";
	}

	public WitherBoss getWitherBoss(GameTeam team)
	{
		return _teamBosses.get(team);
	}

	public Collection<WitherBoss> getWitherBosses()
	{
		return _teamBosses.values();
	}

	public void setDummyBosses(boolean dummyBosses)
	{
		_dummyBosses = dummyBosses;
	}
}
