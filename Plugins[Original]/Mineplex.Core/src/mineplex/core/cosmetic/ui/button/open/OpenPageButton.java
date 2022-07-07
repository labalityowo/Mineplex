package mineplex.core.cosmetic.ui.button.open;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.cosmetic.ui.page.Menu;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.shop.item.IButton;

public abstract class OpenPageButton implements IButton
{
	private Menu _menu;
	private Gadget _active;

	public OpenPageButton(Menu menu, Gadget active)
	{
		_active = active;
		_menu = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType.isLeftClick())
				leftClick(player);
		else
		{
			if (_active != null)
			{
				_menu.playAcceptSound(player);
				_active.disable(player);
				_menu.refresh();
			}
		}
	}

	protected Menu getMenu()
	{
		return _menu;
	}

	protected Gadget getActive()
	{
		return _active;
	}

	protected abstract void leftClick(Player player);
}
