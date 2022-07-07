package nautilus.game.arcade.game.games.skyfall.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkElytraKnockback;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkSlowDown;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

/**
 * Kit which allows Players
 * to use {@link PerkElytraKnockback}.
 *
 * @author xXVevzZXx
 */
public class KitJouster extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkSlowDown("Slow Down", 10),
					new PerkElytraKnockback("Elytra Knockback")
			};

	private static final ItemStack PLAYER_ITEM = new ItemStack(Material.ELYTRA);

	public KitJouster(ArcadeManager manager)
	{
		super(manager, GameKit.SKYFALL_JOUSTER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setChestplate(PLAYER_ITEM);
	}
}
