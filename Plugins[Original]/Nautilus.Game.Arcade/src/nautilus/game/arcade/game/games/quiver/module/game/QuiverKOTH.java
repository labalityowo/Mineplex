package nautilus.game.arcade.game.games.quiver.module.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.module.ModuleCapturePoint;
import nautilus.game.arcade.game.games.quiver.module.ModuleCapturePoint.CaptureState;
import nautilus.game.arcade.game.games.quiver.module.QuiverTeamModule;
import nautilus.game.arcade.scoreboard.GameScoreboard;

public class QuiverKOTH extends QuiverTeamModule
{
	
	private static final long OVERTIME_TIME_INITIAL = 3000;
	private static final long OVERTIME_TIME_DECREASE = 10;

	private ModuleCapturePoint _capturePoint;
	
	private boolean _isOvertime;
	private long _lastNotCaptured;
	private long _overtimeTime;
	
	private Map<GameTeam, Float> _teamScore = new HashMap<>();
	
	public QuiverKOTH(QuiverTeamBase base)
	{
		super(base);
		
		_capturePoint = getBase().getQuiverTeamModule(ModuleCapturePoint.class);
		_overtimeTime = OVERTIME_TIME_INITIAL;
	}
	
	@Override
	public void updateScoreboard()
	{
		GameScoreboard scoreboard = getBase().GetScoreboard();
		
		scoreboard.writeNewLine();
		
		scoreboard.write(C.cGoldB + "Capture Point");
		
		if (_capturePoint.getCapturedTeam() == null)
		{
			scoreboard.write("None");
		}
		else
		{
			scoreboard.write(_capturePoint.getCapturedTeam().GetFormattedName() + "");
		}
				
		if (getBase().IsLive())
		{

			for (GameTeam gameTeam : _teamScore.keySet())
			{
				float percentage = Math.min(_teamScore.get(gameTeam), 100);
				
				scoreboard.writeNewLine();
				scoreboard.write(gameTeam.GetColor() + C.Bold + "Team " + gameTeam.getDisplayName());
				scoreboard.write(percentage + "%");			
			}
		}
		
		scoreboard.writeNewLine();
		scoreboard.draw();
	}

	@Override
	public void setup()
	{
		for (GameTeam gameTeam : getBase().GetTeamList())
		{
			_teamScore.put(gameTeam, 0F);
		}
	}

	@Override
	public void update(UpdateType updateType)
	{
		if (updateType != UpdateType.FAST)
		{
			return;
		}
		
		if (_capturePoint.getCapturedTeam() == null)
		{
			return;
		}
		
		_teamScore.put(_capturePoint.getCapturedTeam(), _teamScore.get(_capturePoint.getCapturedTeam()) + 0.5F);
		
		if (_capturePoint.getState() != CaptureState.CAPTURED)
		{
			_lastNotCaptured = System.currentTimeMillis();
		}
		
		if (_isOvertime)
		{
			_overtimeTime -= OVERTIME_TIME_DECREASE;
		}
		
		for (GameTeam gameTeam : _teamScore.keySet())
		{	
			float percentage = _teamScore.get(gameTeam);
			
			if (percentage >= 100)
			{
				CaptureState state = _capturePoint.getState();
				
				if (_isOvertime && !UtilTime.elapsed(_lastNotCaptured, _overtimeTime))
				{
					Bukkit.broadcastMessage(UtilTime.MakeStr(_overtimeTime - (System.currentTimeMillis() - _lastNotCaptured)));
					return;
				}
				
				if (!_isOvertime && state != CaptureState.CAPTURED)
				{
					_isOvertime = true;
					
					String message = QuiverTeamBase.OVERTIME;
					
					UtilTextMiddle.display(message, "", 10, 30, 10);
					UtilServer.broadcast(message);
					
					_teamScore.put(gameTeam, 99.9F);
					return;
				}
				
				getBase().AnnounceEnd(gameTeam);
				getBase().SetState(GameState.End);
			}
		}
	}

	@Override
	public void finish()
	{
	}

}
