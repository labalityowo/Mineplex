package nautilus.game.arcade.game.games.lobbers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BombToken
{
	public final long Created;
	public final String Thrower;
	public Location A = null;
	public Location B = null;
	public boolean Primed = false;
	
	public BombToken(Player player)
	{
		Thrower = player.getName();
		
		Created = System.currentTimeMillis();
	}
}
