package mineplex.core.treasure.ui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilUI;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.treasure.types.Treasure;

public class TreasurePage extends ShopPageBase<TreasureManager, TreasureShop>
{

	private static final int ELEMENTS_PER_PAGE = 9;
	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(Material.ARROW)
			.setTitle(C.cGreen + "Previous Page")
			.build();
	private static final ItemStack NEXT_PAGE = new ItemBuilder(Material.ARROW)
			.setTitle(C.cGreen + "Next Page")
			.build();

	private final TreasureLocation _treasureLocation;

	private int _page;

	TreasurePage(TreasureManager plugin, TreasureShop shop, Player player, TreasureLocation treasureLocation)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), "Treasure Chest", player);

		_treasureLocation = treasureLocation;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<Treasure> treasures = _treasureLocation.getManager().getTreasures();
		int[] slots = UtilUI.getIndicesFor(ELEMENTS_PER_PAGE, 1, 5, 1);
		int index = 0;
		int start = _page * ELEMENTS_PER_PAGE;

		for (int treasureIndex = start; treasureIndex < start + ELEMENTS_PER_PAGE && treasureIndex < treasures.size(); treasureIndex++)
		{
			Treasure treasure = treasures.get(treasureIndex);

			addButton(slots[index++], prettifyItem(getPlayer(), treasure), new TreasureButton(treasure));
		}

		if (_page != 0)
		{
			addButton(45, PREVIOUS_PAGE, (player, clickType) ->
			{
				_page--;
				refresh();
			});
		}

		if (start + ELEMENTS_PER_PAGE < treasures.size())
		{
			addButton(53, NEXT_PAGE, (player, clickType) ->
			{
				_page++;
				refresh();
			});
		}
	}

	private ItemStack prettifyItem(Player player, Treasure treasure)
	{
		int toOpen = _treasureLocation.getManager().getChestsToOpen(player, treasure);
		boolean canOpen = toOpen > 0;
		ItemStack itemStack = treasure.getTreasureType().getItemStack().clone();
		ItemMeta meta = itemStack.getItemMeta();
		List<String> lore = new ArrayList<>();

		lore.add("");

		if (treasure.isFeaturedChest())
		{
			lore.add(C.cAquaB + "FEATURED CHEST");
			lore.add("");
		}

		lore.add(C.cGray + ChatColor.stripColor(treasure.getTreasureType().getName()) + "s Owned: " + F.elem(toOpen));
		lore.add("");

		for (String description : treasure.getTreasureType().getDescription())
		{
			lore.add(C.cGray + description);
		}

		if (treasure.isPurchasable())
		{
			lore.add("");
			lore.addAll(treasure.getPurchaseMethods());
		}

//		TODO Remove this. Only re-implement if ownership bugs within rewards can be fixed
//		if (!treasure.isDuplicates())
//		{
//			TreasureRewardManager rewardManager = _treasureLocation.getManager().getRewardManager();
//			int total = rewardManager.getTotalItems(treasure);
//			int available = total - rewardManager.getOwnedItems(player, treasure);
//
//			lore.add("");
//			lore.add(C.cGray + "Available Items: " + F.elem(available) + "/" + F.elem(total));
//		}

		if (canOpen)
		{
			lore.add("");
			lore.add(C.cGreen + "Left-Click to open!");
		}
		if (treasure.isPurchasable())
		{
			if (!canOpen)
			{
				lore.add("");
			}

			lore.add(C.cGreen + "Right-Click to purchase!");
		}

		meta.setDisplayName(treasure.getTreasureType().getName());
		meta.setLore(lore);
		itemStack.setItemMeta(meta);

		return itemStack;
	}

	private class TreasureButton implements IButton
	{

		private final Treasure _treasure;

		TreasureButton(Treasure treasure)
		{
			_treasure = treasure;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			// Open
			if (clickType.isLeftClick())
			{
				if (_treasureLocation.openChest(player, _treasure))
				{
					playAcceptSound(player);
				}
				else
				{
					playDenySound(player);
				}
			}
			// Purchase
			else if (_treasure.isPurchasable())
			{
				getShop().openPageForPlayer(player, new PurchaseTreasurePage(getPlugin(), getShop(), getPlayer(), _treasure, TreasurePage.this));
			}
		}
	}
}
