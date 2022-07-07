package mineplex.core.gui.pages;

import java.util.UUID;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.GuiInventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MessageWindow implements Listener, GuiInventory {
	
	private UUID _playersUUID;
	private Plugin _plugin;
	private Inventory _inv;

	public MessageWindow(Plugin plugin, Player player, ItemStack is, String title, int size)
	{
		_plugin = plugin;
		
		this._playersUUID = player.getUniqueId();
		
		_inv = Bukkit.createInventory(null, size, title);
		
		for (int i = 0; i < size; i++) {
			_inv.setItem(i, is);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onClick(InventoryClickEvent e)
	{
		if (!e.getWhoClicked().getUniqueId().equals(_playersUUID))
			return;
		e.setCancelled(true);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e)
	{
		if (!e.getPlayer().getUniqueId().equals(_playersUUID))
			return;
		HandlerList.unregisterAll(this);
		close();
	}

	@Override
	public void openInventory()
	{
		Player player = Bukkit.getPlayer(_playersUUID);
		
		if (player == null || player.getOpenInventory().getTopInventory().equals(_inv))
			return;
		
		UtilPlayer.swapToInventory(player, _inv);
		Bukkit.getPluginManager().registerEvents(this, _plugin);
		open();
	}

	@Override
	public Inventory getInventory()
	{
		return _inv;
	}
	public UUID getPlayersUUID()
	{
		return _playersUUID;
	}
	public Plugin getPlugin()
	{
		return _plugin;
	}
	public void open() {
		
	}
	public void close() {
		
	}
}