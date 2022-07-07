package nautilus.game.arcade.game.games.halloween2016.creatures;

import org.bukkit.Location;
import org.bukkit.entity.Zombie;

import mineplex.core.common.util.C;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobZombie extends CryptBreaker<Zombie>
{
	
	private static float SPEED = 1;
	private static int CRYPT_DAMAGE = 3;
	private static int CRYPT_DAMAGE_COOLDOWN = 20;
	
	private static double HEALTH = 15;

	public MobZombie(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow + "Zombie", Zombie.class, loc, CRYPT_DAMAGE, CRYPT_DAMAGE_COOLDOWN, SPEED);
		
		_extraDamage = 5;
	}
	
	@Override
	public void SpawnCustom(Zombie ent)
	{
		ent.setMaxHealth(HEALTH);
		ent.setHealth(HEALTH);
	}

}
