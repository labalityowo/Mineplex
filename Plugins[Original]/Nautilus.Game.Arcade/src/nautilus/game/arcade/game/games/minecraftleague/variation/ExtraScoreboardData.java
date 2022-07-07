package nautilus.game.arcade.game.games.minecraftleague.variation;

import nautilus.game.arcade.scoreboard.GameScoreboard;

public abstract class ExtraScoreboardData
{
	public GameScoreboard Scoreboard;
	
	public ExtraScoreboardData(GameScoreboard sb)
	{
		Scoreboard = sb;
	}
	
	public abstract void write();
}
