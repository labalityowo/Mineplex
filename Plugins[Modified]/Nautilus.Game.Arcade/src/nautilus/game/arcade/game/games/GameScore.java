package nautilus.game.arcade.game.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.entity.Player;

public class GameScore
{
	public org.bukkit.entity.Player Player;
	public double Score; 
	
	public GameScore(Player player, double i) 
	{
		Player = player;
		Score = i;
	}
	
	public Player GetPlayer()
	{
		return Player;
	}

	public static Comparator<GameScore> SCORE_DESC = new Comparator<GameScore>()
	{
		@Override
		public int compare(GameScore o1, GameScore o2)
		{
			if (o1.Score == o2.Score)
				return 0;

			return o1.Score > o2.Score ? -1 : 1;
		}
	};
}
