package mineplex.core.cosmetic.ui.page;

import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.item.SalesPackageProcessor;

public class ItemGadgetPage extends GadgetPage
{

	public ItemGadgetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player)
	{
		super(plugin, shop, clientManager, donationManager, name, player, GadgetType.ITEM);

		buildPage();
	}

	@Override
	protected void addCustomLore(Gadget gadget, List<String> lore)
	{
		if (gadget instanceof ItemGadget && ((ItemGadget) gadget).isUsingAmmo())
		{
			lore.add(C.blankLine);
			lore.add(C.cWhite + "You own " + C.cGreen + getPlugin().getInventoryManager().Get(getPlayer()).getItemCount(gadget.getName()));
		}
	}

	@Override
	public void purchaseGadget(Player player, Gadget gadget)
	{
		ItemGadget itemGadget = (ItemGadget) gadget;

		if (itemGadget.isUsingAmmo() && itemGadget.getAmmo().getCost(GlobalCurrency.TREASURE_SHARD) <= 0)
		{
			return;
		}

		getShop().openPageForPlayer(
				getPlayer(),
				new ConfirmationPage<>(
						player,
						this,
						new SalesPackageProcessor(
								player,
								GlobalCurrency.TREASURE_SHARD,
								itemGadget.getAmmo(),
								getDonationManager(),
								() ->
								{
									getPlugin().getInventoryManager().addItemToInventory(getPlayer(), itemGadget.getAmmo().getName(), itemGadget.getAmmo().getQuantity());
									refresh();
								}
						),
						gadget.buildIcon()
				)
		);
	}
}
