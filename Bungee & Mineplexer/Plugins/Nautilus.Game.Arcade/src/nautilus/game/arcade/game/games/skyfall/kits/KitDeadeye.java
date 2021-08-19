package nautilus.game.arcade.game.games.skyfall.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkDeadeye;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkSlowDown;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

/**
 * Kit which allows Players
 * to use {@link PerkDeadeye}.
 *
 * @author xXVevzZXx
 */
public class KitDeadeye extends Kit
{

	private static final String DEADEYE = "Dead Eye";

	private static final Perk[] PERKS =
			{
					new PerkSlowDown("Slow Down", 10),
					new PerkDeadeye(DEADEYE, 10)
			};


	private static final ItemStack PLAYER_ITEM = new ItemStack(Material.ELYTRA);

	public KitDeadeye(ArcadeManager manager)
	{
		super(manager, GameKit.SKYFALL_DEADEYE, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setChestplate(PLAYER_ITEM);
	}
}
