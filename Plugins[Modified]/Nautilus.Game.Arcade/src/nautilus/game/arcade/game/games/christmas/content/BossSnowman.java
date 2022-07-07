package nautilus.game.arcade.game.games.christmas.content;

import org.bukkit.Location;
import org.bukkit.entity.Snowman;

public class BossSnowman
{
	public Snowman Entity;
	public Location Spawn;
	public int Direction;
	
	public BossSnowman(Snowman ent, Location loc, int dir)
	{
		Entity = ent;
		Spawn = loc;
		Direction = dir;
	}
}
