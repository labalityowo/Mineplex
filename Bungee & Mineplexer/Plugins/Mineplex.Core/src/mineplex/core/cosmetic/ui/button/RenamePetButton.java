package mineplex.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.cosmetic.ui.page.PetPage;
import mineplex.core.pet.PetType;
import mineplex.core.shop.item.IButton;

public class RenamePetButton implements IButton
{
	private PetPage _page;
	
	public RenamePetButton(PetPage page)
	{
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_page.playAcceptSound(player);
		PetType currentType = _page.getPlugin().getPetManager().getActivePetType(player.getName());
		_page.renamePet(player, currentType, false);
	}
}
