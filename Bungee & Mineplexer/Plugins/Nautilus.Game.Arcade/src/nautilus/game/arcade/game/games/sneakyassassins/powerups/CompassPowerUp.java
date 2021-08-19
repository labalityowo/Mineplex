package nautilus.game.arcade.game.games.sneakyassassins.powerups;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;

public class CompassPowerUp extends PowerUp
{
	protected CompassPowerUp()
	{
		super(PowerUpType.COMPASS);
	}

	@Override
	public boolean powerUpPlayer(Player player, Random random)
	{
		if (player.getInventory().contains(Material.COMPASS))
			return false;

		player.getInventory().addItem(new ItemStack(Material.COMPASS));

		return true;
	}
}
