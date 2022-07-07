package mineplex.core.cosmetic.ui.page;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.event.GadgetChangeEvent;
import mineplex.core.gadget.event.GadgetChangeEvent.GadgetState;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.shop.item.ShopItem;

public class CostumePage extends GadgetPage
{

	CostumePage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player)
	{
		super(plugin, shop, clientManager, donationManager, name, player);

		buildPage();
	}

	protected void buildPage()
	{
		int slot;
		int offset = 0;
		Class<? extends OutfitGadget> lastGadgetClass = null;

		for (Gadget gadget : getPlugin().getGadgetManager().getGadgets(GadgetType.COSTUME))
		{
			if (gadget.isHidden())
			{
				continue;
			}

			OutfitGadget outfitGadget = ((OutfitGadget) gadget);

			if (lastGadgetClass == null || !(gadget.getClass().getSuperclass().isAssignableFrom(lastGadgetClass.getSuperclass())))
			{
				lastGadgetClass = outfitGadget.getClass();
				offset++;
			}

			slot = offset + 18; // 18 = 2 lines down

			switch (outfitGadget.getSlot())
			{
				case CHEST:
					slot += 9;
					break;
				case LEGS:
					slot += 18;
					break;
				case BOOTS:
					slot += 27;
					break;
			}

			addGadget(gadget, slot);

			if (gadget.isActive(getPlayer()))
			{
				addGlow(slot);
			}
		}

		addButton(8, new ShopItem(Material.TNT, C.cRedB + "Remove all Clothing", new String[0], 1, false), (player, clickType) ->
		{
			boolean gadgetDisabled = false;

			for (Gadget gadget : getPlugin().getGadgetManager().getGadgets(GadgetType.COSTUME))
			{
				if (gadget.isActive(player))
				{
					gadgetDisabled = true;
					gadget.disable(player);
					UtilServer.CallEvent(new GadgetChangeEvent(player, gadget, GadgetState.DISABLED));
				}
			}

			if (gadgetDisabled)
			{
				buildPage();
				player.playSound(player.getEyeLocation(), Sound.SPLASH, 1, 1);
			}
		});

		addBackButton();
	}
}
