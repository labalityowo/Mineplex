package mineplex.hub.hubgame.common.general;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

import mineplex.hub.hubgame.HubGame;
import mineplex.hub.hubgame.common.HubGameComponent;

public class InventoryEditComponent extends HubGameComponent<HubGame>
{

	public InventoryEditComponent(HubGame game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void inventoryClick(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();

		if (_game.isAlive(player))
		{
			event.setCancelled(false);
		}
	}
}
