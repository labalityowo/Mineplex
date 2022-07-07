package nautilus.game.arcade.game.modules.capturepoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BeaconInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.missions.GameMissionTracker;

public class CapturePointModule extends Module
{
	
	private final List<CapturePoint> _capturePoints;

	public CapturePointModule()
	{
		_capturePoints = new ArrayList<>(3);
	}

	@Override
	protected void setup()
	{
		getGame().registerMissions(new GameMissionTracker<Game>(MissionTrackerType.GAME_CAPTURE_POINT, getGame())
		{
			@EventHandler
			public void capturePoint(CapturePointCaptureEvent event)
			{
				for (Player player : UtilPlayer.getNearby(event.getPoint().getCenter(), CapturePoint.MAX_RADIUS))
				{
					if (event.getPoint().getOwner().HasPlayer(player))
					{
						_manager.incrementProgress(player, 1, _trackerType, getGameType(), null);
					}
				}
			}
		});
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Entry<String, Location> entry : getLocationStartsWith("POINT").entrySet())
		{
			String[] split = entry.getKey().split(" ");

			if (split.length < 3)
			{
				continue;
			}

			String name = split[1];
			ChatColor colour;

			try
			{
				colour = ChatColor.valueOf(split[2]);
			}
			catch (IllegalArgumentException e)
			{
				continue;
			}

			_capturePoints.add(new CapturePoint(getGame(), name, colour, entry.getValue()));
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !getGame().IsLive())
		{
			return;
		}

		for (CapturePoint point : _capturePoints)
		{
			point.update();
		}
	}

	@EventHandler
	public void beaconInteract(InventoryOpenEvent event)
	{
		if (getGame().IsLive() && event.getInventory() instanceof BeaconInventory)
		{
			event.setCancelled(true);
		}
	}

	public String getDisplayString()
	{
		StringBuilder out = new StringBuilder();

		for (CapturePoint point : _capturePoints)
		{
			out.append(point.getOwner() == null ? C.cWhite : point.getOwner().GetColor()).append(point.getName()).append(" ");
		}

		return out.toString().trim();
	}

	public boolean isOnPoint(Location location)
	{
		for (CapturePoint point : _capturePoints)
		{
			if (point.isOnPoint(location))
			{
				return true;
			}
		}

		return false;
	}

	public List<CapturePoint> getCapturePoints()
	{
		return _capturePoints;
	}

	private Map<String, Location> getLocationStartsWith(String s)
	{
		Map<String, Location> map = new HashMap<>();

		for (String key : getGame().WorldData.GetAllCustomLocs().keySet())
		{
			if (key.startsWith(s))
			{
				map.put(key, getGame().WorldData.GetCustomLocs(key).get(0));
			}
		}

		return map;
	}
}
