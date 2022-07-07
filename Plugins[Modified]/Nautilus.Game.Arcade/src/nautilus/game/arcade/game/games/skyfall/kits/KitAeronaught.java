package nautilus.game.arcade.game.games.skyfall.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkAeronaught;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkSlowDown;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

/**
 * Kit which allows Players
 * to use {@link PerkAeronaught}.
 *
 * @author xXVevzZXx
 */
public class KitAeronaught extends Kit
{

	private static final String DAMAGE = "Elytra Damage";

	private static final Perk[] PERKS =
			{
					new PerkSlowDown("Slow Down", 10),
					new PerkAeronaught(DAMAGE, 0.45)
			};

	private static final ItemStack PLAYER_ITEM = new ItemStack(Material.ELYTRA);

	public KitAeronaught(ArcadeManager manager)
	{
		super(manager, GameKit.SKYFALL_AERONAUGHT, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setChestplate(PLAYER_ITEM);
	}
}
