package mineplex.clanshub.salesannouncements;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class SalesAnnouncementPage implements Listener
{
	private Player _viewer;
	private Inventory _inv;
	private SalesAnnouncementManager _manager;
	private Map<Integer, SalesAnnouncementButton> _buttons = new HashMap<>();
	
	public SalesAnnouncementPage(Player player, SalesAnnouncementManager manager)
	{
		_viewer = player;
		_manager = manager;
		_inv = Bukkit.createInventory(player, 9 * 6, C.cGreen + "All Sales Announcements");
		setup();
		_viewer.openInventory(_inv);
		UtilServer.RegisterEvents(this);
	}
	
	private void setup()
	{
		_buttons.clear();
		int i = 0;
		for (SalesAnnouncementData data : _manager.getLoadedAnnouncements().values())
		{
			_buttons.put(i, new SalesAnnouncementButton(data, this));
			i++;
		}
		updateButtons(false);
	}
	
	private void disable()
	{
		HandlerList.unregisterAll(this);
	}
	
	public void deleteAnnouncement(SalesAnnouncementData data)
	{
		_manager.deleteAnnouncement(_viewer, data, true);
		_manager.runSyncLater(() -> setup(), 2L);
	}
	
	public void toggleAnnouncement(SalesAnnouncementData data)
	{
		_manager.toggleAnnouncement(_viewer, data);
		updateButtons(true);
	}
	
	public void updateButtons(boolean callUpdate)
	{
		_inv.clear();
		_buttons.entrySet().stream().forEach(entry ->
		{
			if (callUpdate)
			{
				entry.getValue().update();
			}
			_inv.setItem(entry.getKey(), entry.getValue().Button);
		});
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
	
	@EventHandler
	public void onListChange(SalesAnnouncementRemoteListUpdateEvent event)
	{
		setup();
	}
}