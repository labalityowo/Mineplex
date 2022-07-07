package nautilus.game.arcade.game.games.cakewars.shop;

import org.bukkit.inventory.ItemStack;

public interface CakeItem
{

	CakeShopItemType getItemType();

	ItemStack getItemStack();

	int getCost();

}
