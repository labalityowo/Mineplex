package nautilus.game.minekart.gp;

import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.track.Track.TrackState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class GPBattle extends GP
{
	public GPBattle(GPManager manager, GPSet trackSet) 
	{
		super(manager, trackSet);
	}

	@Override
	public void UpdateScoreBoard()
	{
		if (GetTrack() == null)
			return;

		if (GetTrack().GetState() == TrackState.Ended)
		{
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard board = manager.getNewScoreboard();

			Objective objective = board.registerNewObjective("showposition", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.AQUA + "Total Score");

			for (Kart kart : GetKarts())
			{		
				if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
					continue;

				ChatColor col = ChatColor.GRAY;
				if (kart.GetLives() == 3)			col = ChatColor.GREEN;
				else if (kart.GetLives() == 2)		col = ChatColor.YELLOW;
				else if (kart.GetLives() == 1)		col = ChatColor.RED;
				
				String name = col + kart.GetDriver().getName();
				if (name.length() > 16)
					name = name.substring(0, 16);

				Score score = objective.getScore(Bukkit.getOfflinePlayer(name));
				score.setScore(GetScore(kart));
			}	

			for (Kart kart : GetKarts())
			{	
				if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
					continue;

				kart.GetDriver().setScoreboard(board);
			}	
		}
		else
		{
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard board = manager.getNewScoreboard();

			Objective objective = board.registerNewObjective("showposition", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.AQUA + "Kart Balloons");

			for (Kart kart : GetKarts())
			{		
				if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
					continue;

				ChatColor col = ChatColor.GRAY;
				if (kart.GetLives() == 3)			col = ChatColor.GREEN;
				else if (kart.GetLives() == 2)		col = ChatColor.YELLOW;
				else if (kart.GetLives() == 1)		col = ChatColor.RED;

				String name = col + kart.GetDriver().getName();
				if (name.length() > 16)
					name = name.substring(0, 16);

				Score score = objective.getScore(Bukkit.getOfflinePlayer(name));
				score.setScore(kart.GetLives());
			}	

			for (Kart kart : GetKarts())
			{	
				if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
					continue;

				kart.GetDriver().setScoreboard(board);
			}	
		}
	}
	
	public void CheckBattleEnd() 
	{
		if (GetTrack().GetState() == TrackState.Loading || GetTrack().GetState() == TrackState.Countdown)
			return;
		
		int alive = 0;
		Kart winner = null;
		for (Kart kart : GetKarts())
		{
			if (kart.GetLives() > 0)
			{
				alive++;
				winner = kart;
			}
		}

		if (alive > 1)
			return;

		if (GetTrack().GetState() != TrackState.Ended)
		{
			this.GetTrack().GetPositions().add(0, winner);
			GetTrack().SetState(TrackState.Ended);
		}
	}
}
