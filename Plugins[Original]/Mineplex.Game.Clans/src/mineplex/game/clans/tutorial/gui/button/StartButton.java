package mineplex.game.clans.tutorial.gui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;
import mineplex.game.clans.tutorial.Tutorial;

public class StartButton implements IButton
{
	private final Tutorial _tutorial;

	public StartButton(Tutorial tutorial)
	{
		_tutorial = tutorial;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (!_tutorial.isInTutorial(player))
		{
			_tutorial.start(player);
		}

		player.closeInventory();
	}
}
