package mineplex.core.gui.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class LoadingWindow implements Runnable, Listener {
	
	public static String[] messages = new String[]{"Sending Carrier Pigeons...", "#BlameChiss", "Converting to Morse Code...", "Training monkeys..."};
	public static long defaultWait = 30;
	
	public ItemStack _background;
	public ItemStack _barLoading;
	public ItemStack _barBack;
	
	private Inventory _inv;
	private final InventoryView _currentInventory;

	private final int _id;

	private final Plugin _plugin;
	private final UUID _playersUUID;

	private String _message;
	private String _title;

	private final int _size;

	private final int[] _activeRows;

	private long _ticker = 0;
	
	public LoadingWindow(Plugin plugin, Player player, int size) {
		this(plugin, player, null, size, null, null);
	}
	
	public LoadingWindow(Plugin plugin, Player player, long startTime, int size) {
		this(plugin, player, startTime, size, null, null);
	}
	
	public LoadingWindow(Plugin plugin, Player player, long startTime, int size, String message)
	{
		this(plugin, player, startTime, size, message, message);
	}
	
	@SuppressWarnings("deprecation")
	public LoadingWindow(Plugin plugin, Player player, Long startTime, int size, String title, String message)
	{
		Validate.notNull(plugin, "The plugin can not be null!");
		Validate.notNull(player, "The player can not be null!");
		
		_currentInventory = player.getOpenInventory();
		
		_size = size;
		_activeRows = getActiveRows(size / 9);
		
		_plugin = plugin;
		_playersUUID = player.getUniqueId();
		
		
		_background = ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData(), 1, _message);
		_barLoading = ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData(), 1, _message);
		_barBack = ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.WHITE.getData(), 1, _message);
		
		
		if (title == null && message == null) 
		{
			String randomName = UtilMath.randomElement(messages);
			title = randomName;
			message = randomName;
		} 
		else if (title == null) 
		{
			title = " ";
		} 
		else if (message == null) 
		{
			message = UtilMath.randomElement(messages);
		}
		if (startTime == null)
			startTime = defaultWait;
		
		
		_title = title;
		setMessage(message);
		_id = Bukkit.getScheduler().runTaskTimer(plugin, this, startTime, 5).getTaskId();
	}
	
	public void setMessage(String message) {
		_message = message;
		ItemMeta im =_background.getItemMeta();
		im.setDisplayName(_message);
		_background.setItemMeta(im);
		_barBack.setItemMeta(im);
		_barLoading.setItemMeta(im);
		
		setBackGround();
		setLoadingBarItems();
	}
	
	public void setTitle(String title) {
		_title = title;
		
		Player player = Bukkit.getPlayer(_playersUUID);
		if (_inv == null || player == null)
			return;
		
		ItemStack[] con = _inv.getContents();
		
		_inv = Bukkit.createInventory(null, _size, _title);
		_inv.setContents(con);
		UtilPlayer.swapToInventory(player, _inv);
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getWhoClicked().getUniqueId().equals(_playersUUID) && event.getInventory().equals(_inv)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void inventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer().getUniqueId().equals(_playersUUID) && event.getInventory().equals(_inv)) {
			end();
		}
	}
	
	@Override
	public void run()
	{
		Player player = Bukkit.getPlayer(_playersUUID);
		
		try
		{
			if (player == null || player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null || _inv == null ? !player.getOpenInventory().equals(_currentInventory) : (!player.getOpenInventory().getTopInventory().equals(_inv))) 
			{
				end();
				return;
			}
			
			if (_inv == null) 
			{
				_inv = Bukkit.createInventory(null, _size, _title);
				
				setBackGround();
				setLoadingBarItems();
				UtilPlayer.swapToInventory(player, _inv);
				Bukkit.getPluginManager().registerEvents(this, _plugin);
			} 
			else
				setLoadingBarItems();
		}
		catch (Exception exception)
		{
			try
			{
				System.out.println("player ? " + (player == null));
				System.out.println("player.getOpenInventory() ? " + (player.getOpenInventory() == null));
				System.out.println("player.getOpenInventory().getTopInventory() ? " + (player.getOpenInventory().getTopInventory() == null));
				System.out.println("_inv ? " + (_inv == null));
				exception.printStackTrace();
			}
			catch (Exception exception2)
			{
				exception.printStackTrace();
			}
			
			end();
		}
		finally
		{
			_ticker++;
		}
	}
	
	public void end() {
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTask(_id);
		_inv = null;
	}
	
	private void setBackGround() {
		if (_inv == null)
			return;
		
		List<Integer> ignore = new ArrayList<Integer>();

		for (int row : _activeRows) {
			
			int rowStart = row * 9;
			int rowEnd = rowStart + 9;
			
			for (int i = rowStart; i < rowEnd; i++) {
				ignore.add(i);
			}
		}
		
		for (int i = 0; i < _size; i++) {
			if (!ignore.contains(i))
				_inv.setItem(i, _background);
		}
	}
	
	private void setLoadingBarItems() {
		if (_inv == null)
			return;
		ItemStack[] loadingBar = getLoadingBar();
		
		for (int row : _activeRows) {
			int rowStart = row * 9;
			for (int i = 0; i < 9; i++) {
				_inv.setItem(i + rowStart, loadingBar[i]);
			}
		}
	}
	
	public ItemStack[] getLoadingBar()
	{
		ItemStack[] loadingBar = new ItemStack[9];

		int barStart = (int) (_ticker % 9);
		int barEnd = (barStart + 3) % 9;
		
		boolean endAfter = barEnd > barStart;
		
		for (int i = 0; i < loadingBar.length; i++) {
			if (endAfter ? (i >= barStart && i < barEnd) : (i >= barStart || i < barEnd))
				loadingBar[i] = _barLoading;
			else
				loadingBar[i] = _barBack;
		}
		return loadingBar;
	}

	private static int[] getActiveRows(int rows)
	{
		float mid = rows / 2.0f;
		if (mid == (int) mid)
			return new int[] { (int) mid , (int) mid -1};
		else
			return new int[] { (int) Math.floor(mid)};
	}
}
