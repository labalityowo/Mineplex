package nautilus.game.arcade.game.games.sneakyassassins.powerups;

import org.bukkit.*;
import org.bukkit.entity.*;

import java.util.*;

public enum PowerUpType
{
	WEAPON(new WeaponPowerUp()),
	ARMOR(new ArmorPowerUp()),
	SMOKE_BOMB(new SmokeBombPowerUp()),
	COMPASS(new CompassPowerUp());

	private final PowerUp _powerUp;

	PowerUpType(PowerUp powerUp)
	{
		_powerUp = powerUp;
	}

	public boolean powerUpPlayer(Player player, Random random)
	{
		return _powerUp.powerUpPlayer(player, random);
	}
}
