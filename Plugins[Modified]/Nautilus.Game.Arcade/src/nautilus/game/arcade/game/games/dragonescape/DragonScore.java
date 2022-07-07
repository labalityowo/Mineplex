package nautilus.game.arcade.game.games.dragonescape;

import org.bukkit.entity.Player;

public class DragonScore 
{
	public org.bukkit.entity.Player Player;
	public double Score;
	
	public DragonScore(Player player, double i) 
	{
		Player = player;
		Score = i;
	}
}
