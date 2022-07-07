package nautilus.game.arcade.game.games.quiver;

import org.bukkit.entity.Player;

public class QuiverScore 
{
	public org.bukkit.entity.Player Player;
	public int Kills;
	
	public QuiverScore(Player player, int i) 
	{
		Player = player;
		Kills = i;
	}
}
