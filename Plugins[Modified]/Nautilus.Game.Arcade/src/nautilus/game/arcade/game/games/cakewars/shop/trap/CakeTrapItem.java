package nautilus.game.arcade.game.games.cakewars.shop.trap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.game.games.cakewars.shop.CakeShopItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopItemType;

public abstract class CakeTrapItem extends CakeShopItem
{

	private final String _name;
	TrapTrigger _trapTrigger;

	CakeTrapItem(ItemStack itemStack, int cost, String name, String... description)
	{
		super(CakeShopItemType.TRAP, new ItemBuilder(itemStack)
				.setTitle(C.cYellowB + name)
				.setLore(UtilText.splitLinesToArray(description, LineFormat.LORE))
				.build(), cost);

		_name = name;
		_trapTrigger = TrapTrigger.CAKE_INTERACT;
	}

	public abstract void onTrapTrigger(Player player, Location cake);

	public String getName()
	{
		return _name;
	}

	public TrapTrigger getTrapTrigger()
	{
		return _trapTrigger;
	}

	public enum TrapTrigger
	{
		CAKE_NEAR,
		CAKE_INTERACT,
	}
}
