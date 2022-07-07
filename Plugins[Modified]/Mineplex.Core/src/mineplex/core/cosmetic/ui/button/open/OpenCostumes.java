package mineplex.core.cosmetic.ui.button.open;

import org.bukkit.entity.Player;

import mineplex.core.cosmetic.ui.page.Menu;
import mineplex.core.gadget.types.Gadget;

public class OpenCostumes extends OpenPageButton
{
	public OpenCostumes(Menu menu, Gadget active)
	{
		super(menu, active);
	}

	@Override
	protected void leftClick(Player player)
	{
		getMenu().openCostumes(player);
	}
}
