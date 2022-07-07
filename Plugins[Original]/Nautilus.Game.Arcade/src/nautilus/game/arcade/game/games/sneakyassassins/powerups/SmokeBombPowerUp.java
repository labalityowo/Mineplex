package nautilus.game.arcade.game.games.sneakyassassins.powerups;

import nautilus.game.arcade.game.games.sneakyassassins.kits.*;
import org.bukkit.entity.*;

import java.util.*;

public class SmokeBombPowerUp extends PowerUp
{
	protected SmokeBombPowerUp()
	{
		super(PowerUpType.SMOKE_BOMB);
	}

	@Override
	public boolean powerUpPlayer(Player player, Random random)
	{
		return player.getInventory().addItem(SneakyAssassinKit.SMOKE_BOMB.clone()).isEmpty();
	}
}
