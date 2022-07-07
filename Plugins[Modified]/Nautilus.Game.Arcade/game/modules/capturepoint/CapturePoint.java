package nautilus.game.arcade.game.modules.capturepoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;

public class CapturePoint
{

	static final int MAX_RADIUS = 5;
	private static final int MAX_PROGRESS = 5;
	private static final int MAX_PROGRESS_NEUTRAL = 10;
	private static final int MIN_INFORM_TIME = (int) TimeUnit.SECONDS.toMillis(30);

	private final Game _host;

	private final String _name;
	private final ChatColor _colour;

	private final Location _center;
	private final List<Block> _wool;
	private final List<Block> _changed;

	private final double _captureDist;

	private GameTeam _owner;
	private GameTeam _side;
	private int _progress;
	private long _lastInform;

	CapturePoint(Game host, String name, ChatColor colour, Location center)
	{
		_host = host;
		_name = name;
		_colour = colour;
		_center = center;
		_wool = new ArrayList<>(36);
		_changed = new ArrayList<>(_wool.size());

		double highestDist = 0;

		for (Entry<Block, Double> entry : UtilBlock.getInRadius(center, MAX_RADIUS).entrySet())
		{
			Block block = entry.getKey();
			double offset = entry.getValue();

			if (block.getType() != Material.WOOL)
			{
				continue;
			}

			if (offset > highestDist)
			{
				highestDist = offset;
			}

			_wool.add(block);
		}
		Collections.shuffle(_wool);

		_captureDist = Math.pow(highestDist * (double) MAX_RADIUS + 0.5D, 2);
	}

	public void update()
	{
		// Store the number of players in a team in this map
		Map<GameTeam, Integer> playersOnPoint = new HashMap<>();

		for (GameTeam team : _host.GetTeamList())
		{
			// Populate
			playersOnPoint.put(team, 0);
			int players = 0;

			for (Player player : team.GetPlayers(true))
			{
				// Ignore for spectators
				// If they are not in the range
				if (UtilPlayer.isSpectator(player) || !isOnPoint(player.getLocation()))
				{
					continue;
				}

				// Increment
				players++;
			}

			// Put in map
			playersOnPoint.put(team, players);
		}

		// For each team get the team with the non-zero players
		GameTeam highest = null;
		int highestPlayers = 0;
		for (Entry<GameTeam, Integer> entry : playersOnPoint.entrySet())
		{
			GameTeam team = entry.getKey();
			int players = entry.getValue();

			// Only care if people are on it
			if (players > 0)
			{
				// If this is the first team on the point
				if (highest == null)
				{
					highest = team;
					highestPlayers = players;
				}
				// This means there are 2 teams on the point
				else
				{
					return;
				}
			}
		}

		// No one at all is on the point
		if (highest == null)
		{
			if (_owner == null)
			{
				return;
			}

			// If the owner isn't null, move the point's progress back
			highest = _owner;
			highestPlayers = 1;
		}
		// Players on the point
		// Only inform if it has been a while
		else if ((_owner == null || !_owner.equals(highest)) && UtilTime.elapsed(_lastInform, MIN_INFORM_TIME))
		{
			_lastInform = System.currentTimeMillis();

			String message = F.main("Game", "Team " + highest.GetFormattedName() + C.mBody + " is capturing the " + _colour + _name + C.mBody + " Beacon!");

			sendMessage(highest, message);

			if (_owner != null)
			{
				sendMessage(_owner, message);
			}
		}

		// If it has just reached the maximum progress, set the owner.
		if (_owner != null && _owner.equals(highest) && _progress >= (_owner == null ? MAX_PROGRESS_NEUTRAL : MAX_PROGRESS))
		{
			return;
		}

		capture(highest, highestPlayers);
	}

	private void sendMessage(GameTeam team, String message)
	{
		team.GetPlayers(true).forEach(player ->
		{
			player.playSound(player.getLocation(), Sound.GHAST_SCREAM2, 1, 1);
			player.sendMessage(message);
		});
	}

	private void capture(GameTeam team, int progress)
	{
		// No player has ever stood on the point
		if (_side == null)
		{
			_side = team;
		}

		// If it is the same team
		if (_side.equals(team))
		{
			// Increase progress
			_progress += progress;
			display(team, progress, true);

			// Captured
			if (_progress >= (_owner == null ? MAX_PROGRESS_NEUTRAL : MAX_PROGRESS))
			{
				_progress = MAX_PROGRESS;
				setOwner(team);
			}
		}
		// Other team
		else
		{
			// Point back to a neutral state
			if (_progress <= 0)
			{
				setBeaconColour(null);
				_side = team;
				_progress = 0;
				// Recursively call this method now that the first (same team) condition will be true
				capture(team, progress);
				return;
			}

			_progress -= progress;
			display(team, progress, false);
		}
	}

	private void setOwner(GameTeam team)
	{
		setBeaconColour(team);

		// Same team no need to inform
		if (_owner != null && _owner.equals(team))
		{
			return;
		}
		else
		{
			// As the point is easier to capture after the initial capture
			// We need to adjust the current progress, otherwise it has to go
			// from 10 to 0 then to 5 which is unintended
			_progress = MAX_PROGRESS;
		}

		String message = F.main("Game", "Team " + team.GetFormattedName() + C.mBody + " captured the " + _colour + _name + C.mBody + " Beacon!");

		if (_owner != null)
		{
			sendMessage(_owner, message);
		}
		sendMessage(team, message);

		_owner = team;

		UtilFirework.playFirework(_center, Type.BURST, team.GetColorBase(), false, false);
		UtilServer.CallEvent(new CapturePointCaptureEvent(this));
	}

	private void display(GameTeam team, int progress, boolean forward)
	{
		double toChange = Math.ceil(_wool.size() / (_owner == null ? MAX_PROGRESS_NEUTRAL : MAX_PROGRESS)) * progress + 1;
		int changed = 0;
		for (Block block : _wool)
		{
			if (changed >= toChange)
			{
				return;
			}

			Block glass = block.getRelative(BlockFace.UP);

			if (forward)
			{
				if (_changed.contains(block))
				{
					continue;
				}

				block.setData(team.GetColorData());
				glass.setData(team.GetColorData());
				changed++;
				_changed.add(block);
			}
			else
			{
				if (!_changed.contains(block))
				{
					continue;
				}

				block.setData((byte) 0);
				glass.setData((byte) 0);
				changed++;
				_changed.remove(block);
			}

			glass.getWorld().playEffect(glass.getLocation().add(0.5, 0.5, 0.5), Effect.STEP_SOUND, block.getType(), team.GetColorData());
		}
	}

	private void setBeaconColour(GameTeam team)
	{
		byte colour = team == null ? 0 : team.GetColorData();

		_center.getBlock().getRelative(BlockFace.DOWN).setData(colour);
	}

	public boolean isOnPoint(Location location)
	{
		return UtilMath.offsetSquared(_center, location) < _captureDist;
	}

	public String getName()
	{
		return _name;
	}

	public ChatColor getColour()
	{
		return _colour;
	}

	public GameTeam getOwner()
	{
		return _owner;
	}

	public Location getCenter()
	{
		return _center.clone();
	}
}
