package mineplex.gemhunters.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.gemhunters.death.event.PlayerCustomRespawnEvent;

@ReflectivelyCreateMiniPlugin
public class InventoryModule extends MiniPlugin
{

	public static final ItemStack LOCKED = new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 15).setTitle(C.cGray + "Locked").build();
	private static final int START_INDEX = 9;
	private static final String ITEM_METADATA = "UNLOCKER";

	private final LootModule _loot;

	private final Map<UUID, Integer> _slotsUnlocked;

	private InventoryModule()
	{
		super("Unlocker");

		_loot = require(LootModule.class);

		_slotsUnlocked = new HashMap<>();
	}

	@EventHandler
	public void respawn(PlayerCustomRespawnEvent event)
	{
		resetSlots(event.getPlayer());
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		_slotsUnlocked.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();

		if (event.getClickedInventory() == null || event.getCurrentItem() == null)
		{
			return;
		}

		if (event.getCurrentItem().isSimilar(LOCKED))
		{
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0.6F);
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null)
		{
			return;
		}

		LootItem lootItem = _loot.fromItemStack(itemStack);

		if (lootItem == null || lootItem.getMetadata() == null || !lootItem.getMetadata().equals(ITEM_METADATA))
		{
			return;
		}

		player.setItemInHand(UtilInv.decrement(itemStack));
		unlockSlots(player, itemStack.getType() == Material.CHEST ? 9 : 18);
	}

	public void unlockSlots(Player player, int slots)
	{
		unlockSlots(player, slots, true);
	}

	public void unlockSlots(Player player, int slots, boolean inform)
	{
		_slotsUnlocked.putIfAbsent(player.getUniqueId(), 0);

		Inventory inv = player.getInventory();
		UUID key = player.getUniqueId();

		int start = START_INDEX + _slotsUnlocked.get(key);
		int end = Math.min(inv.getSize(), start + slots);
		int delta = end - start;

		for (int i = start; i < end; i++)
		{
			inv.setItem(i, null);
		}

		if (inform)
		{
			player.sendMessage(F.main(_moduleName, "You unlocked an additional " + F.count(String.valueOf(delta)) + " slots of your inventory!"));
		}
		_slotsUnlocked.put(key, _slotsUnlocked.get(key) + slots);
	}

	public void resetSlots(Player player)
	{
		Inventory inv = player.getInventory();

		_slotsUnlocked.put(player.getUniqueId(), 0);

		for (int i = START_INDEX; i < inv.getSize(); i++)
		{
			inv.setItem(i, LOCKED);
		}
	}

	public int getSlots(Player player)
	{
		return _slotsUnlocked.get(player.getUniqueId());
	}
}
