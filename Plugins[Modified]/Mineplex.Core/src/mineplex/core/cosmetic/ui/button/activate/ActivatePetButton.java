package mineplex.core.cosmetic.ui.button.activate;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.cosmetic.ui.page.Menu;
import mineplex.core.cosmetic.ui.page.PetPage;
import mineplex.core.pet.PetType;
import mineplex.core.shop.item.IButton;

public class ActivatePetButton implements IButton
{
	private PetType _pet;
	private PetPage _page;
	
	public ActivatePetButton(PetType pet, PetPage page)
	{
		_pet = pet;
		_page = page;
	}
	
	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_page.playAcceptSound(player);
		_page.getPlugin().getPetManager().addPetOwner(player, _pet, player.getLocation());
		_page.getShop().openPageForPlayer(player, new Menu(_page.getPlugin(), _page.getShop(), _page.getClientManager(), _page.getDonationManager(), player));
	}
}
