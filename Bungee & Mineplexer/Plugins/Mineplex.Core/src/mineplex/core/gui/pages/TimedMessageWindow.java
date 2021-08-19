package mineplex.core.gui.pages;

import mineplex.core.gui.GuiInventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class TimedMessageWindow extends MessageWindow implements Runnable {
	private GuiInventory _gui;
	private int _id;
	private long _time;
	
	public TimedMessageWindow(Plugin plugin, Player player, ItemStack is, String title, int size, long time)
	{
		this(plugin, player, is, title, size, time, null);
	}

	public TimedMessageWindow(Plugin plugin, Player player, ItemStack is, String title, int size, long time, GuiInventory gui)
	{
		super(plugin, player, is, title, size);
		
		this._gui = gui;
		this._time = time;
	}
	
	@Override
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		super.onClick(e);
		if (e.isCancelled() && _gui != null && (e.getWhoClicked() instanceof Player)) {
			_gui.openInventory();
		}
	}
	
	@Override
	public void open()
	{
		this._id = Bukkit.getScheduler().runTaskLater(getPlugin(), this, _time).getTaskId();
	}
	
	@Override
	public void close()
	{
		Bukkit.getScheduler().cancelTask(_id);
	}
	
	@Override
	public void run()
	{
		Player player = Bukkit.getPlayer(getPlayersUUID());
		if (player == null)
			return;

		if (_gui != null) {
			_gui.openInventory();
		} else
			player.closeInventory();
	}

}
