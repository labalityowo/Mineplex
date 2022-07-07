package mineplex.game.clans.clans.boxes;

import java.util.function.Consumer;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.boxes.extra.BuilderBox;

public class BoxManager extends MiniPlugin
{
	private final BoxShop _shop;
	
	public BoxManager(JavaPlugin plugin)
	{
		super("Box Manager", plugin);
		
		_shop = new BoxShop(this);
	}
	
	public void openDyePage(Player player)
	{
		_shop.attemptShopOpen(player);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onCraftWithDye(PrepareItemCraftEvent event)
	{
		if (event.getInventory().getResult() == null)
		{
			return;
		}
		if (event.getInventory().getResult().getType() == Material.LAPIS_BLOCK)
		{
			for (ItemStack item : event.getInventory().getMatrix())
			{
				if (item == null)
				{
					continue;
				}
				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(C.cGold + "Dye"))
				{
					event.getInventory().setResult(null);
					return;
				}
			}
			return;
		}
		if (event.getInventory().getResult().getType() == Material.INK_SACK)
		{
			event.getInventory().setResult(null);
			return;
		}
		for (ItemStack item : event.getInventory().getMatrix())
		{
			if (item == null)
			{
				continue;
			}
			if (item.getType() != Material.INK_SACK)
			{
				continue;
			}
			if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(C.cGold + "Dye"))
			{
				event.getInventory().setResult(null);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onCraftWithDye(CraftItemEvent event)
	{
		if (event.getInventory().getResult() == null)
		{
			return;
		}
		if (event.getInventory().getResult().getType() == Material.LAPIS_BLOCK)
		{
			for (ItemStack item : event.getInventory().getMatrix())
			{
				if (item == null)
				{
					continue;
				}
				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(C.cGold + "Dye"))
				{
					event.setCancelled(true);
					return;
				}
			}
			return;
		}
		if (event.getInventory().getResult().getType() == Material.INK_SACK)
		{
			event.setCancelled(true);
			return;
		}
		for (ItemStack item : event.getInventory().getMatrix())
		{
			if (item == null)
			{
				continue;
			}
			if (item.getType() != Material.INK_SACK)
			{
				continue;
			}
			if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(C.cGold + "Dye"))
			{
				event.setCancelled(true);
			}
		}
	}
	  
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlaceDyeInAnvil(InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
		{
			return;
		}
		if (!event.getInventory().getType().equals(InventoryType.ANVIL))
		{
			return;
		}
		if (!(event.getCursor() != null && event.getCursor().hasItemMeta() && event.getCursor().getItemMeta().hasDisplayName() && event.getCursor().getItemMeta().getDisplayName().equals(C.cGold + "Dye")) && !(event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equals(C.cGold + "Dye")))
		{
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasDisplayName())
		{
			return;
		}
		if (event.getItem().getItemMeta().getDisplayName().equals(C.cGold + "Dye"))
		{
			event.setCancelled(true);
		}
	}
	
	public static enum BoxType
	{
		BUILDER_BOX("Clans Builder Box", C.cGold + "Builder's Box", Material.GLOWSTONE, BuilderBox::open),
		@SuppressWarnings("deprecation")
		DYE_BOX(null, C.cGreen + "Dye Box", Material.INK_SACK, DyeColor.RED.getDyeData(), Managers.get(BoxManager.class)::openDyePage),
		;
		
		private String _itemName, _displayName;
		private ItemBuilder _displayBuilder;
		private Consumer<Player> _itemGenerator;
		
		private BoxType(String itemName, String displayName, Material displayMaterial, Consumer<Player> itemGenerator)
		{
			_itemName = itemName;
			_displayName = displayName;
			_displayBuilder = new ItemBuilder(displayMaterial).setTitle(displayName).addLore(C.cRed);
			_itemGenerator = itemGenerator;
		}
		
		private BoxType(String itemName, String displayName, Material displayMaterial, short data, Consumer<Player> itemGenerator)
		{
			_itemName = itemName;
			_displayName = displayName;
			_displayBuilder = new ItemBuilder(displayMaterial).setData(data).setTitle(displayName).addLore(C.cRed);
			_itemGenerator = itemGenerator;
		}
		
		public String getItemName()
		{
			return _itemName;
		}
		
		public String getDisplayName()
		{
			return _displayName;
		}
		
		public ItemStack getDisplayItem(int owned)
		{
			ItemBuilder newBuilder = new ItemBuilder(_displayBuilder.build());
			if (owned == -1)
			{
				return newBuilder.build();
			}
			if (owned > 0)
			{
				newBuilder.setGlow(true);
			}
			return newBuilder.addLore(C.cGreenB + "Owned: " + C.cWhite + owned).build();
		}
		
		public void onUse(Player player)
		{
			_itemGenerator.accept(player);
		}
	}
}