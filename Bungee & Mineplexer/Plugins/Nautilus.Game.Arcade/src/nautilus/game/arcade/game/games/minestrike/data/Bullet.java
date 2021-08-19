package nautilus.game.arcade.game.games.minestrike.data;

import java.util.HashSet;

import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.guns.Gun;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;

public class Bullet
{
	public Player Shooter;
	public Entity Bullet;
	public Gun Gun;
	public Location Origin;
	public Vector Direction;
	
	public long LastSound = System.currentTimeMillis() - 100;
	
	public HashSet<Player> WhizzSound = new HashSet<Player>();
	
	public Bullet(Entity bullet, Gun gun, Player shooter, GunModule game)
	{
		Bullet = bullet;
		Gun = gun;
		Origin = shooter.getEyeLocation();
		Direction = shooter.getLocation().getDirection();
		Shooter = shooter;
	}
	
	public boolean isValid()
	{
		return Bullet.isValid();
	}

	public double getDamage()
	{
		return Gun.getDamage();
	}
	
	public double getDamageDropoff(Location destination)
	{
		return Math.max(-Gun.getDamage() + 0.5, -Gun.getDamage() * (Gun.getDropOff() * UtilMath.offset(Origin, destination)));
	}

	public boolean bulletSound()
	{
		if (UtilTime.elapsed(LastSound, 250))
		{
			LastSound = System.currentTimeMillis();
			return true;
		}
		
		return false;
	}
}
