package nautilus.game.arcade.game.games.gladiators.hotbar;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;

/**
 * Created by William (WilliamTiger).
 * 18/12/15
 */
public class HotbarInventory
{
	public static void open(Player player, HotbarEditor editor)
	{
		Inventory inv = UtilServer.getServer().createInventory(null, 36, "Hotbar Editor");

		for (int slot : Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 31, 33, 34, 35))
		{
			inv.setItem(slot, getGlass(slot));
		}

		HotbarLayout layout = editor.getLayout(player);

		inv.setItem(layout.getSword() + 9, new ItemStack(Material.DIAMOND_SWORD, 1));
		inv.setItem(layout.getRod() + 9, new ItemStack(Material.FISHING_ROD, 1));
		inv.setItem(layout.getBow() + 9, new ItemStack(Material.BOW, 1));
		inv.setItem(layout.getArrows() + 9, new ItemStack(Material.ARROW, 1));

		inv.setItem(30, new ItemBuilder(Material.EMERALD_BLOCK).setTitle(C.cGreen + C.Bold + "Save").setLore(C.cGray + "Click to save layout.").build());
		inv.setItem(32, new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cRed + C.Bold + "Cancel").setLore(C.cGray + "Click to cancel layout.").build());

		player.openInventory(inv);
	}

	private static ItemStack getGlass(int slot)
	{
		return new ItemBuilder(Material.STAINED_GLASS_PANE)
				.setData((short) 15)
				.setTitle((slot < 10 ? C.cAquaB + "⬇ Arrange Your Hotbar ⬇" : C.cAquaB + "⬆ Arrange Your Hotbar ⬆"))
				.build();
	}
}
