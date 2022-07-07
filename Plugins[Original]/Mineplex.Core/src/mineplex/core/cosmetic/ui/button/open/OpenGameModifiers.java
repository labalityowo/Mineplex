package mineplex.core.cosmetic.ui.button.open;

import org.bukkit.entity.Player;

import mineplex.core.cosmetic.ui.page.Menu;
import mineplex.core.cosmetic.ui.page.gamemodifiers.GameCosmeticsPage;
import mineplex.core.gadget.types.Gadget;

public class OpenGameModifiers extends OpenPageButton
{

	public OpenGameModifiers(Menu menu, Gadget active)
	{
		super(menu, active);
	}

	@Override
	protected void leftClick(Player player)
	{
		getMenu().getShop().openPageForPlayer(player, new GameCosmeticsPage(getMenu().getPlugin(), getMenu().getShop(), getMenu().getClientManager(), getMenu().getDonationManager(),player));
	}
}