package nautilus.game.arcade.kit.perks.data;

import java.util.HashSet;

import org.bukkit.entity.Player;

public class ReboundData 
{
	public Player Shooter;
	public HashSet<Player> Ignore = new HashSet<Player>();
	public int Bounces;
	
	public ReboundData(Player shooter, int bounces, HashSet<Player> previousIgnore)
	{
		Shooter = shooter;
		Bounces = bounces;
		
		if (previousIgnore != null)
			Ignore = previousIgnore;
		
		Ignore.add(shooter);
	}
}
