package mineplex.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.shop.item.IButton;

public class GadgetButton implements IButton
{
	private Gadget _gadget;
	private GadgetPage _page;
	
	public GadgetButton(Gadget gadget, GadgetPage page)
	{
		_gadget = gadget;
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_page.purchaseGadget(player, _gadget);
	}
}
