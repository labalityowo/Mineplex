package nautilus.game.arcade.game.games.sneakyassassins.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkRevealer;
import nautilus.game.arcade.kit.perks.PerkSmokebomb;

public class KitRevealer extends SneakyAssassinKit
{

	private static final Perk[] PERKS =
			{
					new PerkSmokebomb(Material.INK_SACK, 3, true),
					new PerkRevealer()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.DIAMOND, (byte) 0, 3,
							C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Reveal Assassins",
							new String[]{
									ChatColor.RESET + "Throw a revealer.",
									ChatColor.RESET + "Players within 5 blocks",
									ChatColor.RESET + "get revealed for 5 seconds.",
							})
			};

	public KitRevealer(ArcadeManager manager)
	{
		super(manager, GameKit.SNEAKY_ASSASSINS_REVEALER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		super.GiveItems(player);

		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
