package nautilus.game.arcade.game.games.sneakyassassins.powerups;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;

public class WeaponPowerUp extends PowerUp
{
	private static final List<Material> SWORD_PROGRESSION = Arrays.asList(
			Material.WOOD_SWORD,
			Material.STONE_SWORD,
			Material.GOLD_SWORD,
			Material.IRON_SWORD,
			Material.DIAMOND_SWORD
	);

	public WeaponPowerUp()
	{
		super(PowerUpType.WEAPON);
	}

	@Override
	public boolean powerUpPlayer(Player player, Random random)
	{
		for (int swordType = 0; swordType < SWORD_PROGRESSION.size(); swordType++)
		{
			int swordSlot = player.getInventory().first(SWORD_PROGRESSION.get(swordType));

			if (swordSlot != -1)
			{
				int newSwordType = swordType + 1;

				if (newSwordType < SWORD_PROGRESSION.size())
				{
					player.getInventory().setItem(swordSlot, new ItemStack(SWORD_PROGRESSION.get(newSwordType)));

					return true;
				}
			}
		}

		return false;
	}
}
