package mineplex.core.communities.gui.pages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.gui.buttons.CommunitiesGUIButton;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public abstract class CommunitiesGUIPage implements Listener
{
	protected Player Viewer;
	protected Inventory Inv;
	protected Map<Integer, CommunitiesGUIButton> Buttons = new HashMap<>();

	protected static CommunityManager _manager;
	
	public CommunitiesGUIPage(String name, int rows, Player viewer)
	{
		Viewer = viewer;
		Inv = Bukkit.createInventory(viewer, 9 * rows, name);
	}
	
	private void disable()
	{
		HandlerList.unregisterAll(this);
	}
	
	public static CommunityManager getCommunityManager()
	{
		if (_manager == null)
		{
			_manager = Managers.require(CommunityManager.class);
		}
		return _manager;
	}
	
	public void open()
	{
		Viewer.openInventory(Inv);
		UtilServer.RegisterEvents(this);
	}
	
	public void updateButtons(boolean callUpdate)
	{
		Inv.clear();
		Buttons.entrySet().stream().forEach(entry ->
		{
			if (callUpdate)
			{
				entry.getValue().update();
			}
			Inv.setItem(entry.getKey(), entry.getValue().Button);
		});
		Viewer.updateInventory();
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			if (Viewer.getOpenInventory() == null || Viewer.getOpenInventory().getTopInventory() == null || !Viewer.getOpenInventory().getTopInventory().getTitle().equals(Inv.getTitle()))
			{
				disable();
				return;
			}
		}
		if (event.getType() == UpdateType.SEC_05)
		{
			Buttons.entrySet().forEach(entry ->
			{
				entry.getValue().update();
				Inv.setItem(entry.getKey(), entry.getValue().Button);
			});
		}
	}
	
	@EventHandler
	public void handleClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null || !event.getClickedInventory().getTitle().equals(Inv.getTitle()))
		{
			return;
		}
		if (!Viewer.getName().equals(event.getWhoClicked().getName()))
		{
			return;
		}
		event.setCancelled(true);
		if (!Recharge.Instance.use(Viewer, "Communities Button Click", 500, false, false))
		{
			return;
		}
		Integer slot = event.getSlot();
		if (!Buttons.containsKey(slot))
		{
			return;
		}
		Buttons.get(slot).handleClick(event.getClick());
	}
}