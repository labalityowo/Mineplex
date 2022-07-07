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
import nautilus.game.arcade.kit.perks.PerkSmokebomb;

public class KitBriber extends SneakyAssassinKit
{

	private static final Perk[] PERKS =
			{
					new PerkSmokebomb(Material.INK_SACK, 3, true),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 4,
							C.cYellow + C.Bold + "Right-Click Villager" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bribe Villager",
							new String[]{
									ChatColor.RESET + "Pay a villager to help you.",
									ChatColor.RESET + "It will attack the nearest",
									ChatColor.RESET + "enemy for 15 seconds.",
							})
			};

	public KitBriber(ArcadeManager manager)
	{
		super(manager, GameKit.SNEAKY_ASSASSINS_BRIBER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		super.GiveItems(player);

		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
