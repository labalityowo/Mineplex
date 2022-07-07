package mineplex.core.shop.page;


import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilInv;
import mineplex.core.donation.DonationManager;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.item.IButton;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class ShopPageBase<PluginType extends Lifetimed, ShopType extends ShopBase<PluginType>> extends CraftInventoryCustom implements Listener
{
	protected PluginType _plugin;
	protected CoreClientManager _clientManager;
	protected DonationManager _donationManager;
	protected ShopType _shop;
	protected Player _player;
	protected CoreClient _client;
	protected NautHashMap<Integer, IButton> _buttonMap;
	protected boolean _showCurrency = false;
	
	private int _currencySlot = 4;
	
	public ShopPageBase(PluginType plugin, ShopType shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player) 
	{
		this(plugin, shop, clientManager, donationManager, name, player, 54);
	}
	
	public ShopPageBase(PluginType plugin, ShopType shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, int slots) 
	{
		super(null, slots, name);
		
		_plugin = plugin;
		_clientManager = clientManager;
		_donationManager = donationManager;
		_shop = shop;
		_player = player;
		_buttonMap = new NautHashMap<>();
		
		_client = _clientManager.Get(player);
	}

	protected abstract void buildPage();

	protected int getSlot(int rowOffset, int columnOffset)
	{
		return (rowOffset * 9) + columnOffset;
	}

	protected void addItem(int slot, ItemStack item)
	{
		if (slot > inventory.getSize() - 1)
		{
			_player.getInventory().setItem(getPlayerSlot(slot), item);
		}
		else
		{
			setItem(slot, item);
		}
	}

	protected void addItemFakeCount(int slot, ItemStack item, int fakeCount)
	{
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		nmsStack.count = fakeCount;

		if (slot > inventory.getSize() - 1)
		{
			((CraftPlayer) _player).getHandle().inventory.setItem(getPlayerSlot(slot), nmsStack);
		}
		else
		{
			getInventory().setItem(slot, nmsStack);
		}
	}
	
	protected int getPlayerSlot(int slot)
	{
		return slot >= (inventory.getSize() + 27) ? slot - (inventory.getSize() + 27) : slot - (inventory.getSize() - 9);
	}
	
	public void addButton(int slot, ItemStack item, IButton button)
	{
		if (button == null)
		{
			addButtonNoAction(slot, item);
			return;
		}

		addItem(slot, item);
		
		_buttonMap.put(slot, button);
	}

	protected void addButtonNoAction(int slot, ItemStack item)
	{
		addItem(slot, item);
	}

	protected void addButtonFakeCount(int slot, ItemStack item, IButton button, int fakeItemCount)
	{
		addItemFakeCount(slot, item, fakeItemCount);

		_buttonMap.put(slot, button);
	}

	protected void addGlow(int slot)
	{
		UtilInv.addDullEnchantment(getItem(slot));
	}
	
	protected void removeButton(int slot)
	{
		getInventory().setItem(slot, null);
		_buttonMap.remove(slot);
	}

	public void playerClicked(InventoryClickEvent event)
	{
		ClickType clickType = event.getClick();
		boolean shiftClick = (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT);
		int rawSlot = event.getRawSlot(); 
		
		if (_buttonMap.containsKey(rawSlot))
		{
			_buttonMap.get(event.getRawSlot()).onClick(_player, event.getClick());
			event.setCancelled(true);
		}
		else if ((rawSlot >= 0 && rawSlot < inventory.getSize()) || shiftClick)
		{
			playDenySound(_player);
			event.setCancelled(true);
		} 
	}
	
	public boolean matchesInventory(Inventory newInventory)
	{
		return getName().equalsIgnoreCase(newInventory.getName());
	}
	
	public void playerOpened()
	{
		
	}

	public void playerClosed()
	{
		this.inventory.onClose((CraftPlayer) _player);
	}
	
	public void playAcceptSound(Player player)
	{
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.6f);
	}
	
	public void playRemoveSound(Player player)
	{
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0.6f);
	}

	public void playDenySound(Player player)
	{
		player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
	}
	
	public void dispose()
	{
		_player = null;
		_client = null;
		_shop = null;
		_plugin = null;
	}

	public void refresh()
	{
		clearPage();
		buildPage();
	}

	public void clearPage()
	{
		clear();
		_buttonMap.clear();
	}

	public void setItem(int column, int row, ItemStack itemStack)
	{
		setItem(column + (row * 9), itemStack);
	}
	
	public ShopType getShop()
	{
		return _shop;
	}
	
	public PluginType getPlugin()
	{
		return _plugin;
	}
	
	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}
	
	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	protected Player getPlayer()
	{
		return _player;
	}

	public CoreClient getClient()
	{
		return _client;
	}

	protected NautHashMap<Integer, IButton> getButtonMap()
	{
		return _buttonMap;
	}

	protected boolean shouldShowCurrency()
	{
		return _showCurrency;
	}

	protected int getCurrencySlot()
	{
		return _currencySlot;
	}
}
