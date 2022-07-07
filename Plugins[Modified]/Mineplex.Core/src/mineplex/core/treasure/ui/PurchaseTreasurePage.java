package mineplex.core.treasure.ui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.SalesPackageBase;
import mineplex.core.shop.item.SalesPackageProcessor;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.treasure.ChestPackage;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.treasure.types.Treasure;

public class PurchaseTreasurePage extends ShopPageBase<TreasureManager, TreasureShop>
{

	private static final ItemStack GO_BACK = new ItemBuilder(Material.BED)
			.setTitle(C.cGreen + "Go Back")
			.build();
	private static final ItemStack NO_REWARDS = new ItemBuilder(Material.REDSTONE_BLOCK)
			.setTitle(C.cRedB + "No Rewards")
			.addLore("", "You already own all of the items that", "can appear in this chest.", "Thus you cannot purchase this chest anymore.")
			.build();

	private final Treasure _treasure;
	private final TreasurePage _previous;
	private final int _available;

	PurchaseTreasurePage(TreasureManager plugin, TreasureShop shop, Player player, Treasure treasure, TreasurePage previous)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), treasure.getTreasureType().getName(), player);

		_treasure = treasure;
		_previous = previous;
		_available = treasure.isDuplicates()
				? Integer.MAX_VALUE
				: plugin.getRewardManager().getTotalItems(treasure) - plugin.getRewardManager().getOwnedItems(player, treasure) - plugin.getChestsToOpen(player, treasure);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addButton(4, GO_BACK, (player, clickType) -> getShop().openPageForPlayer(player, _previous));

		int purchaseAmount = 1;

		if (addButton(20, purchaseAmount))
		{
			addButtonNoAction(22, NO_REWARDS);
			removeButton(20);
			return;
		}

		purchaseAmount = 5;

		if (addButton(22, purchaseAmount))
		{
			return;
		}

		purchaseAmount = 10;

		addButton(24, purchaseAmount);
	}

	private boolean addButton(int slot, int purchaseAmount)
	{
		boolean overflow = false;

		if (purchaseAmount > _available)
		{
			purchaseAmount = _available;
			overflow = true;

			if (purchaseAmount == 1 || purchaseAmount == 5)
			{
				return true;
			}
		}

		addButton(slot, getPurchaseItem(purchaseAmount), new PurchaseChestButton(purchaseAmount));
		return overflow;
	}

	private ItemStack getPurchaseItem(int amount)
	{
		ItemStack itemStack = _treasure.getTreasureType().getItemStack().clone();
		ItemMeta meta = itemStack.getItemMeta();
		List<String> lore = new ArrayList<>();

		lore.add("");
		lore.add(C.cGray + (amount == 1 ? "Buy" : "Bulk buy") + " " + F.elem(amount) + " chest" + (amount == 1 ? "" : "s") + ".");
		lore.add(C.cGray + "This will cost " + F.currency(GlobalCurrency.TREASURE_SHARD, _treasure.getPurchasePrice() * amount) + "!");
		lore.add("");
		lore.add(C.cGreen + "Click to purchase!");

		meta.setDisplayName(_treasure.getTreasureType().getName());
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		itemStack.setAmount(amount);

		return itemStack;
	}

	private class PurchaseChestButton implements IButton
	{

		private final int _amount;

		PurchaseChestButton(int amount)
		{
			_amount = amount;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			getPlugin().purchase(newAmount ->
			{
				SalesPackageBase salesPackage = new ChestPackage(_treasure.getTreasureType().getItemName(), Material.CHEST, _treasure.getPurchasePrice() * newAmount);

				getShop().openPageForPlayer(
						player,
						new ConfirmationPage<>(
								player,
								_previous,
								new SalesPackageProcessor(
										player,
										GlobalCurrency.TREASURE_SHARD,
										salesPackage,
										getDonationManager(),
										() ->
										{
											getPlugin().giveTreasure(player, _treasure, newAmount);
											_previous.buildPage();
											player.sendMessage(F.main(getPlugin().getName(), "Purchased " + F.elem(newAmount) + " " + F.name(_treasure.getTreasureType().getName()) + "."));
										}),
								salesPackage.buildIcon()));
			}, player, _treasure, _amount);
		}
	}
}
