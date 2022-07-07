package nautilus.game.arcade.game.games.halloween2016.creatures;

import org.bukkit.Location;
import org.bukkit.entity.Blaze;

import mineplex.core.common.util.C;

import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobBlaze extends CryptBreaker<Blaze>
{
	
	private static int CRYPT_DAMAGE = 10;
	private static int CRYPT_RATE = 30;
	
	private static float SPEED = 1;

	public MobBlaze(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow + "Blaze", Blaze.class, loc, CRYPT_DAMAGE, CRYPT_RATE, SPEED);
	}

}
