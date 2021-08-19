package nautilus.game.arcade.game.games.sneakyassassins.powerups;

import java.util.Random;

import nautilus.game.arcade.game.games.sneakyassassins.kits.*;
import org.bukkit.*;
import org.bukkit.entity.*;

public abstract class PowerUp
{
	private final PowerUpType _powerUpType;

	protected PowerUp(PowerUpType powerUpType)
	{
		_powerUpType = powerUpType;
	}

	public PowerUpType getPowerUpType()
	{
		return _powerUpType;
	}

	public abstract boolean powerUpPlayer(Player player, Random random);
}
