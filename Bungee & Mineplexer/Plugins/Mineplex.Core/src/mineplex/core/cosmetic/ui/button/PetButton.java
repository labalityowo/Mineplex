package mineplex.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.cosmetic.ui.page.PetPage;
import mineplex.core.pet.PetType;
import mineplex.core.shop.item.IButton;

public class PetButton implements IButton
{
	private PetType _petType;
	private PetPage _page;
	
	public PetButton(PetType pet, PetPage page)
	{
		_petType = pet;
		_page = page;
	}

	public void onClick(Player player, ClickType clickType)
	{
		_page.purchasePet(player, _petType);
	}
}
