package nautilus.game.arcade.game.games.castleassault.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.GameTeam;

public class CapturePoint 
{
	private static final int POINTS_TO_CAPTURE = 100;
	private static final long TIME_PER_POINT = 1000;
	private static final int POINTS_PER_TICK = 1;
	
	private Location _loc;
	
	private long _lastCap;
	private int _points = 0;
	private GameTeam _owner = null;

	public CapturePoint(GameTeam owner, Location loc)
	{
		_owner = owner;

		_loc = loc;
	}
	
	public int getMaxPoints()
	{
		return POINTS_TO_CAPTURE;
	}
	
	public int getPoints()
	{
		return _points;
	}
	
	public boolean isCaptured()
	{
		return _points >= POINTS_TO_CAPTURE;
	}

	public void update()
	{
		if (!UtilTime.elapsed(_lastCap, TIME_PER_POINT))
		{
			return;
		}
		
		int capping = 0;
		for (Player player : UtilPlayer.getInRadius(_loc, 3.5).keySet())
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}
			if (_owner.HasPlayer(player))
			{
				continue;
			}
			capping++;
		}
		
		if (capping > 0 && _points < POINTS_TO_CAPTURE)
		{
			_lastCap = System.currentTimeMillis();
			_points += POINTS_PER_TICK;
		}
	}
}