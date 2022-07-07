package nautilus.game.arcade.game.games.moba.shop;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.Menu;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.Moba;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class MobaShopCategoryMenu extends Menu<ArcadeManager>
{

	private static final int SLOTS = 27;
	private static final int STARTING_SLOT = 10;
	private static final ItemStack GO_BACK_ITEM = new ItemBuilder(Material.BED)
			.setTitle(C.cGreen + "Go Back")
			.build();

	private final Moba _host;
	private final MobaShop _shop;
	private final MobaShopCategory _category;

	public MobaShopCategoryMenu(Moba host, MobaShop shop, MobaShopCategory category, ArcadeManager plugin)
	{
		super(category.getName() + " Category", plugin);

		_host = host;
		_shop = shop;
		_category = category;
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[SLOTS];
		int slot = STARTING_SLOT;

		buttons[4] = new GoBackButton(getPlugin());

		for (MobaItem item : _category.getItems())
		{
			ItemBuilder builder = new ItemBuilder(item.getItem());
			boolean owns = _shop.ownsItem(player, item) && _category.isTrackingPurchases();
			boolean canPurchase = _host.getGoldManager().hasGold(player, item.getCost());
			int gold = _host.getGoldManager().getGold(player);

			builder.setTitle((canPurchase ? C.cGreen : C.cRed) + item.getItem().getItemMeta().getDisplayName());

			if (owns)
			{
				builder.setType(Material.WOOL);
				builder.setData((byte) 5);
				builder.addLore(C.cRed + "You already have purchased this upgrade.");
			}
			else
			{
				builder.addLore(C.cWhite + "Cost: " + C.cGold + item.getCost(), C.cWhite + "Your Gold: " + C.cGold + gold, "");

				if (canPurchase)
				{
					builder.addLore(C.cGreen + "Click to purchase.");
				}
				else
				{
					builder.addLore(C.cRed + "You cannot afford this item.");
				}
			}

			buttons[slot++] = new MobaPurchaseButton(builder.build(), getPlugin(), item);

			// Reached the end of the row, wrap it to keep it neat.
			if (slot == 17)
			{
				slot = 19;
			}
		}

		return buttons;
	}

	class MobaPurchaseButton extends Button<ArcadeManager>
	{

		private MobaItem _item;

		public MobaPurchaseButton(ItemStack itemStack, ArcadeManager plugin, MobaItem item)
		{
			super(itemStack, plugin);

			_item = item;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			boolean owns = _shop.ownsItem(player, _item) && _category.isTrackingPurchases();
			boolean canPurchase = _host.getGoldManager().hasGold(player, _item.getCost());

			if (!owns && canPurchase)
			{
				player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.6F);
				_shop.purchaseItem(player, _item);
				player.closeInventory();
			}
			else
			{
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0.6F);
			}
		}
	}

	class GoBackButton extends Button<ArcadeManager>
	{

		public GoBackButton(ArcadeManager plugin)
		{
			super(GO_BACK_ITEM, plugin);
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			_shop.openShop(player);
		}
	}
}
