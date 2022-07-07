package mineplex.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for the Menu system
 */
@Deprecated
public class MenuListener implements Listener
{

	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		String name = event.getInventory().getName();
		Player player = (Player) event.getWhoClicked();
		Menu gui = Menu.get(player.getUniqueId());

		if (gui == null)
		{
			return;
		}

		if (!gui.getName().equalsIgnoreCase(name))
		{
			return;
		}

		Button button = gui.getButton(event.getRawSlot());

		event.setCancelled(true);
		event.setResult(Event.Result.DENY);

		if (button == null)
		{
			return;
		}

		if(button.useItemClick())
		{
			button.onClick(player, event.getClick(), event.getCurrentItem());
			return;
		}

		button.onClick(player, event.getClick());
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event)
	{
		String name = event.getInventory().getName();
		Player player = (Player) event.getPlayer();
		Menu gui = Menu.get(player.getUniqueId());

		if (gui == null)
		{
			return;
		}

		if (!gui.getName().equalsIgnoreCase(name))
		{
			return;
		}

		if(gui.isUseClose())
		{
			gui.onClose(player);
			gui.setUseClose(false);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Menu.remove(event.getPlayer().getUniqueId());
	}

}
