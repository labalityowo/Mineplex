package mineplex.core.shop;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.donation.DonationManager;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;
import mineplex.core.npc.event.NpcDamageByEntityEvent;
import mineplex.core.npc.event.NpcInteractEntityEvent;
import mineplex.core.shop.page.ShopPageBase;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class ShopBase<PluginType extends Lifetimed> extends ListenerComponent
{
	private NautHashMap<String, Long> _errorThrottling;
	private NautHashMap<String, Long> _purchaseBlock;
	
	private PluginType _plugin;
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private String _name;
	private Map<UUID, ShopPageBase<PluginType, ? extends ShopBase<PluginType>>> _playerPageMap = new HashMap<>();
	
	private Set<UUID> _openedShop = new HashSet<>();
	
	public ShopBase(PluginType plugin, CoreClientManager clientManager, DonationManager donationManager, String name)
	{		
		_plugin = plugin;
		_clientManager = clientManager;
		_donationManager = donationManager;
		_name = name;

		_errorThrottling = new NautHashMap<String, Long>();
		_purchaseBlock = new NautHashMap<String, Long>();

		_plugin.getLifetime().register(this);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDamageEntity(NpcDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
		{
    		if (attemptShopOpen((Player) event.getDamager(), event.getNpc()))
    		{
    			event.setCancelled(true);
    		}	    			
		}
	}
	
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(NpcInteractEntityEvent event)
    {
		if (attemptShopOpen(event.getPlayer(), (LivingEntity) event.getNpc()))
			event.setCancelled(true);
    }
    
    private boolean attemptShopOpen(Player player, LivingEntity entity)
    {
    	if (entity.isCustomNameVisible() && entity.getCustomName() != null && ChatColor.stripColor(entity.getCustomName()).equalsIgnoreCase(ChatColor.stripColor(_name)))
    	{
    		return attemptShopOpen(player);
    	}
		
		return false;
    }
    
    public boolean attemptShopOpen(Player player)
    {
		if (!_openedShop.contains(player.getUniqueId()))
		{
			if (!canOpenShop(player))
				return false;
			
			_openedShop.add(player.getUniqueId());
			
    		openShopForPlayer(player);
    		if (!_playerPageMap.containsKey(player.getUniqueId()))
    		{
    			_playerPageMap.put(player.getUniqueId(), buildPagesFor(player));
    		}
    		
    		openPageForPlayer(player, getOpeningPageForPlayer(player));
    		
    		return true;
		}
		
		return false;
    }
    
	protected ShopPageBase<PluginType, ? extends ShopBase<PluginType>> getOpeningPageForPlayer(HumanEntity player)
	{
		return _playerPageMap.get(player.getUniqueId());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (isPlayerInShop(event.getWhoClicked()))
		{
			ShopPageBase<?,?> page = getOpeningPageForPlayer(event.getWhoClicked());
			
			if (page.matchesInventory(event.getInventory()))
			{
				page.playerClicked(event);
				
				if (event.getRawSlot() < page.getSize())
				{
					event.setCancelled(true);	
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event)
	{
		if (isPlayerInShop(event.getWhoClicked()))
		{
			ShopPageBase<?,?> page = getOpeningPageForPlayer(event.getWhoClicked());
			
			if (page.matchesInventory(event.getInventory()))
			{
				event.setCancelled(true);	// Prevent players from dragging items into NPC shop inventories
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (_playerPageMap.containsKey(event.getPlayer().getUniqueId()) && _playerPageMap.get(event.getPlayer().getUniqueId()).getTitle() != null && _playerPageMap.get(event.getPlayer().getUniqueId()).getTitle().equalsIgnoreCase(event.getInventory().getTitle()))
		{
			removePlayer((Player) event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent event)
	{
		if (!event.isCancelled())
			return;

		if (_playerPageMap.containsKey(event.getPlayer().getUniqueId()) && _playerPageMap.get(event.getPlayer().getUniqueId()).getTitle() != null && _playerPageMap.get(event.getPlayer().getUniqueId()).getTitle().equalsIgnoreCase(event.getInventory().getTitle()))
		{
			removePlayer((Player) event.getPlayer());
		}
	}

	protected void removePlayer(Player player)
	{
		_playerPageMap.get(player.getUniqueId()).playerClosed();
		_playerPageMap.get(player.getUniqueId()).dispose();

		_playerPageMap.remove(player.getUniqueId());

		closeShopForPlayer(player);

		_openedShop.remove(player.getUniqueId());
	}

	@Override
	public void deactivate()
	{
		super.deactivate();
		_playerPageMap.entrySet().stream().map(Map.Entry::getKey).map(Bukkit::getPlayer).forEach(p -> {
			if (_playerPageMap.get(p.getName()).getTitle().equals(p.getOpenInventory().getTitle()))
			{
				p.closeInventory();
			}
			removePlayer(p);
		});
		_playerPageMap.clear();
		_openedShop.clear();
		_purchaseBlock.clear();
		_errorThrottling.clear();
	}

	protected boolean canOpenShop(Player player)
	{
		return true;
	}
	
	protected void openShopForPlayer(Player player) { }
	
	protected void closeShopForPlayer(Player player) { }
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (_playerPageMap.containsKey(event.getPlayer().getUniqueId()))
		{
			removePlayer(event.getPlayer());
			event.getPlayer().closeInventory();
		}
	}

	public void openPageForPlayer(Player player, ShopPageBase<PluginType, ? extends ShopBase<PluginType>> page)
	{
		if (_playerPageMap.containsKey(player.getUniqueId()))
		{
			_playerPageMap.get(player.getUniqueId()).playerClosed();
		}			
		
		setCurrentPageForPlayer(player, page);

	    EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
	    if (nmsPlayer.activeContainer != nmsPlayer.defaultContainer)
	    {
	        // Do this so that other inventories know their time is over.
	        CraftEventFactory.handleInventoryCloseEvent(nmsPlayer);
	        nmsPlayer.m();
	    }
		player.openInventory(page);
	}
	
	public void setCurrentPageForPlayer(Player player, ShopPageBase<PluginType, ? extends ShopBase<PluginType>> page)
	{
		_playerPageMap.put(player.getUniqueId(), page);
	}
	
	public void addPlayerProcessError(Player player)
	{
		if (_errorThrottling.containsKey(player.getName()) && (System.currentTimeMillis() - _errorThrottling.get(player.getName()) <= 5000))
			_purchaseBlock.put(player.getName(), System.currentTimeMillis());

		_errorThrottling.put(player.getName(), System.currentTimeMillis());
	}

	public boolean canPlayerAttemptPurchase(Player player)
	{
		return !_purchaseBlock.containsKey(player.getName()) || (System.currentTimeMillis() - _purchaseBlock.get(player.getName()) > 10000);
	}
	
	public Map<UUID, ShopPageBase<PluginType, ? extends ShopBase<PluginType>>> getPageMap()
	{
		return _playerPageMap;
	}
	
	protected abstract ShopPageBase<PluginType, ? extends ShopBase<PluginType>> buildPagesFor(Player player);

	public boolean isPlayerInShop(HumanEntity player)
	{
		return _playerPageMap.containsKey(player.getUniqueId());
	}

	protected PluginType getPlugin()
	{
		return _plugin;
	}

	protected CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	protected DonationManager getDonationManager()
	{
		return _donationManager;
	}

	protected String getName()
	{
		return _name;
	}

	protected Map<UUID, ShopPageBase<PluginType, ? extends ShopBase<PluginType>>> getPlayerPageMap()
	{
		return _playerPageMap;
	}

	protected Set<UUID> getOpenedShop()
	{
		return _openedShop;
	}
}
