package nautilus.game.arcade.game.games.moba.minion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.structure.tower.Tower;
import nautilus.game.arcade.game.games.moba.structure.tower.TowerDestroyEvent;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;

public class MinionManager implements Listener
{
	public enum Perm implements Permission
	{
		DEBUG_REMOVEMINIONS_COMMAND,
	}

	private static final long MINION_SPAWN_TIME = TimeUnit.SECONDS.toMillis(30);

	private final Moba _host;

	private List<Location> _path;
	private final Set<MinionWave> _waves;

	private long _lastWave;
	private boolean _enabled;

	public MinionManager(Moba host)
	{
		_host = host;
		_waves = new HashSet<>();
		_enabled = true;

		host.registerDebugCommand("removeminions", Perm.DEBUG_REMOVEMINIONS_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			for (MinionWave wave : _waves)
			{
				wave.cleanup();
			}

			caller.sendMessage(F.main("Debug", "Removed all minions."));
		});
	}

	@EventHandler
	public void spawnMinions(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_enabled || !_host.IsLive() || !UtilTime.elapsed(_lastWave, MINION_SPAWN_TIME) || _waves.size() > 6)
		{
			return;
		}

		_lastWave = System.currentTimeMillis();

		if (_path == null)
		{
			preparePath();
		}

		for (GameTeam team : _host.GetTeamList())
		{
			List<Location> path = new ArrayList<>(_path);
			boolean reverse = team.GetColor() == ChatColor.RED;
			boolean superMinions = true;

			for (Tower tower : _host.getTowerManager().getTowers())
			{
				if (!tower.isDead() && !tower.getOwner().equals(team))
				{
					superMinions = false;
					break;
				}
			}

			// If red team, reverse the pat
			if (reverse)
			{
				Collections.reverse(path);
			}

			MinionWave wave = new MinionWave(_host, this, team, path, reverse ? Zombie.class : PigZombie.class, superMinions);

			_waves.add(wave);
		}
	}

	@EventHandler
	public void towerDestroy(TowerDestroyEvent event)
	{
		Tower tower = event.getTower();

		if (tower.isFirstTower())
		{
			return;
		}

		for (GameTeam team : _host.GetTeamList())
		{
			if (team.equals(tower.getOwner()))
			{
				continue;
			}

			_host.Announce(F.main("Game", team.GetFormattedName() + "'s " + C.mBody + "minions are now " + C.cYellowB + "Super-Charged" + C.mBody + "!"), false);
		}
	}

	public void disableMinions()
	{
		_enabled = false;
	}

	public void unregisterWave(MinionWave wave)
	{
		for (Minion minion : wave.getMinions())
		{
			minion.getEntity().remove();
		}

		_waves.remove(wave);
	}

	public Set<MinionWave> getWaves()
	{
		return _waves;
	}

	/**
	 * This method fills the {@link #_path} with the organised list of locations that the minions must follow.<br>
	 * <p>
	 * This says that the blue team is the start and the red team is the end.
	 */
	private void preparePath()
	{
		// Step 1 - Find the starting location for the blue team
		Location start = _host.WorldData.GetDataLocs(MobaConstants.MINION_PATH_START).get(0);

		// Step 2 - Fill a list with ordered locations, from blue to red
		ArrayList<Location> path = new ArrayList<>(_host.WorldData.GetDataLocs(MobaConstants.MINION_PATH));
		ArrayList<Location> organisedPath = new ArrayList<>(path.size());

		while (!path.isEmpty())
		{
			Location dataPoint = UtilAlg.findClosest(start, path);

			organisedPath.add(dataPoint);
			path.remove(dataPoint);
			start = dataPoint;
		}

		// Step 3 - Put the ordered path inside the map
		_path = organisedPath;

//		int i = 0;
//		for (Location location : _path)
//		{
//			Block block = location.getBlock();
//			block.setType(Material.SIGN_POST);
//			Sign sign = (Sign) block.getState();
//			sign.setLine(0, "P" + i++);
//			sign.update();
//		}
	}

	public List<Location> getPath(boolean redTeam)
	{
		List<Location> path = new ArrayList<>(_path);

		if (redTeam)
		{
			Collections.reverse(path);
		}

		return path;
	}
}
