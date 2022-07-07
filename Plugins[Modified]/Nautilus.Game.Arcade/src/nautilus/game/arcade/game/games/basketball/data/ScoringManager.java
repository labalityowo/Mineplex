package nautilus.game.arcade.game.games.basketball.data;

import org.bukkit.ChatColor;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.basketball.Basketball;
import nautilus.game.arcade.scoreboard.GameScoreboard;

/**
 * Manager to store gameplay scores and dynamically display them wherever needed
 */
public class ScoringManager
{
	private Basketball _host;
	private int _redScore = 0;
	private int _blueScore = 0;
	
	public ScoringManager(Basketball host)
	{
		_host = host;
	}
	
	/**
	 * Getter for the current scores of both teams
	 * @return The scores of each team, with the order of red then blue
	 */
	public Integer[] getScores()
	{
		return new Integer[] {_redScore, _blueScore};
	}
	
	/**
	 * Displays the scores and time remaining on the Game Scoreboard
	 * @param sb The GameScoreboard of the currently running Basketball game
	 */
	public void displayScores(GameScoreboard sb, boolean end, String winTeam)
	{
		sb.reset();
		sb.write(C.cRedB + "Red Score");
		sb.write(C.cWhite + getScores()[0]);
		
		sb.writeNewLine();
		
		sb.write(C.cAquaB + "Blue Score");
		sb.write(C.cWhite + getScores()[1]);
		
		sb.writeNewLine();
		
		if (end)
		{
			sb.write(C.cWhiteB + "FINAL SCORE");
			sb.writeNewLine();
			sb.write(winTeam + " Wins!");
		}
		else
		{
			sb.write(C.cYellowB + "Time Remaining");
			long timeLeft = (_host.GetStateTime() + UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.MILLISECONDS)) - System.currentTimeMillis();
			if (!_host.IsLive() && !_host.InProgress())
			{
				timeLeft = 0;
			}
			else if (!_host.IsLive() && _host.InProgress())
			{
				timeLeft = UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.SECONDS);
			}

			if (timeLeft < 0)
			{
				sb.write(C.cYellow + "Overtime");
			}
			else
			{
				sb.write(UtilTime.MakeStr(timeLeft));
			}
		}
		
		sb.draw();
	}
	
	/**
	 * Adds points to a team
	 * @param team The team to add points to
	 * @param points The amount of points to add
	 */
	public void addPoint(GameTeam team, int points)
	{
		if (team.GetColor() == ChatColor.RED)
		{
			_redScore += points;
		}
		if (team.GetColor() == ChatColor.AQUA)
		{
			_blueScore += points;
		}
	}
}
