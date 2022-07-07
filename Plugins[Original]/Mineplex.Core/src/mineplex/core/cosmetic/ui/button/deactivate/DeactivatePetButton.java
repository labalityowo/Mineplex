package mineplex.core.cosmetic.ui.button.deactivate;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.pet.PetManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;

public class DeactivatePetButton implements IButton
{
	private ShopPageBase<?,?> _page;
	private PetManager _petManager;
	
	public DeactivatePetButton(ShopPageBase<?,?> page, PetManager petManager)
	{
		_page = page;
		_petManager = petManager;
	}
	
	public void onClick(Player player, ClickType clickType)
	{
		_page.playAcceptSound(player);
		_petManager.removePet(player, true);
		_page.refresh();
	}
}
