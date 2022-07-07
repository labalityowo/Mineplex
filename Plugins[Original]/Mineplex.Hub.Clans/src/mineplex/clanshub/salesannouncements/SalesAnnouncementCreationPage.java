package mineplex.clanshub.salesannouncements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import mineplex.core.Managers;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class SalesAnnouncementCreationPage implements Listener
{
	private Player _viewer;
	private Inventory _inv;
	private String _message;
	private Map<Integer, SalesAnnouncementGUIButton> _buttons = new HashMap<>();
	public List<PermissionGroup> Selected = new ArrayList<>();
	
	public SalesAnnouncementCreationPage(Player player, String message)
	{
		_viewer = player;
		_message = message;
		_inv = Bukkit.createInventory(player, 9 * 4, C.cGreen + "Select Ranks");
		setup();
		_viewer.openInventory(_inv);
		UtilServer.RegisterEvents(this);
	}
	
	private void setup()
	{
		int slot = 0;
		for (PermissionGroup group : PermissionGroup.values())
		{
			if (group.canBePrimary())
			{
				_buttons.put(slot++, new RankSelectionButton(group, this));
			}
		}
		_buttons.put(31, new RankSelectionFinalizeButton(this));
		updateButtons(false);
	}
	
	private void disable()
	{
		HandlerList.unregisterAll(this);
	}
	
	public void finalizeSelection()
	{
		Managers.get(SalesAnnouncementManager.class).createAnnouncement(_viewer, Selected.toArray(new PermissionGroup[Selected.size()]), _message);
		Managers.get(SalesAnnouncementManager.class).runSyncLater(() -> _viewer.closeInventory(), 1L);
	}
	
	public void updateButtons(boolean callUpdate)
	{
		_inv.clear();
		_buttons.entrySet().stream().filter(entry -> entry.getKey() != 31).forEach(entry ->
		{
			if (callUpdate)
			{
				entry.getValue().update();
			}
			_inv.setItem(entry.getKey(), entry.getValue().Button);
		});
		if (callUpdate)
		{
			_buttons.get(31).update();
		}
		_inv.setItem(31, _buttons.get(31).Button);
		_viewer.updateInventory();
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if (event.getType() == UpdateType.TICK)
		{
			if (_viewer.getOpenInventory() == null || _viewer.getOpenInventory().getTopInventory() == null || !_viewer.getOpenInventory().getTopInventory().getTitle().equals(_inv.getTitle()))
			{
				disable();
				return;
			}
		}
	}
	
	@EventHandler
	public void handleClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null || !event.getClickedInventory().getTitle().equals(_inv.getTitle()))
		{
			return;
		}
		if (!_viewer.getName().equals(event.getWhoClicked().getName()))
		{
			return;
		}
		event.setCancelled(true);
		Integer slot = event.getSlot();
		if (!_buttons.containsKey(slot))
		{
			return;
		}
		_buttons.get(slot).handleClick(event.getClick());
	}
}