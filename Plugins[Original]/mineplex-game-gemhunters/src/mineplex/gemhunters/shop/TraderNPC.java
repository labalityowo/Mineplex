package mineplex.gemhunters.shop;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.util.SimpleNPC;

public class TraderNPC extends SimpleNPC
{
	private final EconomyModule _economy;

	private final VillagerProperties _properties;
	private final Set<TradeableItem> _selling;
	private final Inventory _inv;

	private final long _spawnedAt;

	public TraderNPC(JavaPlugin plugin, Location spawn, Class<? extends LivingEntity> type, String name, boolean vegetated, VillagerProperties properties, Set<TradeableItem> selling)
	{
		super(plugin, spawn, type, name, null, vegetated);

		_economy = Managers.require(EconomyModule.class);

		_properties = properties;
		_selling = selling;
		_inv = plugin.getServer().createInventory(null, 9, name);
		_spawnedAt = System.currentTimeMillis();
		
		int index = 1;

		for (TradeableItem item : _selling)
		{
			ItemStack itemStack = new ItemBuilder(item.getLootItem().getItemStack()).addLore("Cost: " + F.currency(GlobalCurrency.GEM, item.getCost())).build();
			
			_inv.setItem(index++, itemStack);
		}
	}

	@Override
	@EventHandler
	public void npcClick(PlayerInteractEntityEvent event)
	{
		super.npcClick(event);

		if (event.getRightClicked().equals(_entity))
		{
			event.setCancelled(true);
			event.getPlayer().openInventory(_inv);
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (event.getInventory() == null)
		{
			return;
		}

		if (!event.getInventory().equals(_inv))
		{
			return;
		}

		event.setCancelled(true);
		
		ItemStack itemStack = event.getCurrentItem();

		if (itemStack == null)
		{
			return;
		}

		Player player = (Player) event.getWhoClicked();
		int gems = _economy.getGems(player);
		int cost = fromItemStack(itemStack);

		if (cost == 0)
		{
			return;
		}

		if (cost > gems)
		{
			player.sendMessage(F.main("Shop", "I'm sorry you don't have enough gems to purchase this."));
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0.6F);
			return;
		}
		
		if (!UtilInv.HasSpace(player, itemStack.getType(), itemStack.getAmount()))
		{
			player.sendMessage(F.main("Shop", "I'm sorry you don't have enough space to hold that."));
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0.6F);
			return;
		}

		_economy.removeFromStore(player, cost);

		// Remove cost lore
		ItemBuilder builder = new ItemBuilder(itemStack);
		
		List<String> lore = builder.getLore();
		lore.remove(lore.size() - 1);
		builder.setLore(lore.toArray(new String[0]));
		
		itemStack = builder.build();
		
		String itemName = ItemStackFactory.Instance.GetName(itemStack, true);
		
		player.sendMessage(F.main("Shop", "Purchased " + F.elem(itemName) + "!"));
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.2F);
		player.getInventory().addItem(itemStack);
	}

	public int fromItemStack(ItemStack itemStack)
	{
		for (TradeableItem item : _selling)
		{
			ItemStack itemStack2 = item.getLootItem().getItemStack();
			
			if (itemStack.getType() == itemStack2.getType())
			{
				return item.getCost();
			}
		}

		return 0;
	}

	public final VillagerProperties getProperties()
	{
		return _properties;
	}

	public final long getSpawnedAt()
	{
		return _spawnedAt;
	}

}
