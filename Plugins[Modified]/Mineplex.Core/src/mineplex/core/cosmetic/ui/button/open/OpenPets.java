package mineplex.core.cosmetic.ui.button.open;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.cosmetic.ui.page.Menu;
import mineplex.core.cosmetic.ui.page.PetPage;
import mineplex.core.shop.item.IButton;

public class OpenPets implements IButton
{
	private Menu _menu;

	public OpenPets(Menu menu)
	{
		_menu = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType.isLeftClick())
			_menu.getShop().openPageForPlayer(player, new PetPage(_menu.getPlugin(), _menu.getShop(), _menu.getClientManager(), _menu.getDonationManager(), "Pets", player));
		else
		{
			_menu.playAcceptSound(player);
			_menu.getPlugin().getPetManager().removePet(player, true);
			_menu.refresh();
		}
	}
}