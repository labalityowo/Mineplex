package nautilus.game.arcade.game.games.gladiators.hotbar;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/**
 * Created by William (WilliamTiger).
 * 18/12/15
 */
public class HotbarPageListener implements Listener
{
	private HotbarEditor _editor;

	public HotbarPageListener(HotbarEditor editor)
	{
		_editor = editor;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		if (!e.getInventory().getName().equals("Hotbar Editor"))
			return;

		if (e.getCurrentItem() == null)
			return;

		if (e.getAction().equals(InventoryAction.HOTBAR_SWAP) || e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)){
			e.setCancelled(true);
			return;
		}

		if (e.getClick().isShiftClick())
		{
			e.setCancelled(true);
			return;
		}

		System.out.println(e.getClickedInventory().getName() + " : " + e.getInventory().getName());

		if (e.getCurrentItem().getType().equals(Material.REDSTONE_BLOCK))
		{
			e.setCancelled(true);
			e.getWhoClicked().closeInventory();
			return;
		}

		if (e.getClickedInventory() instanceof PlayerInventory || e.getInventory() instanceof PlayerInventory){
			e.setCancelled(true);
			return;
		}

		if (e.getCurrentItem().getType().equals(Material.EMERALD_BLOCK))
		{
			if (!e.getAction().equals(InventoryAction.PICKUP_ALL)){
				e.setCancelled(true);
				return;
			}

			e.setCancelled(true);
			e.getWhoClicked().closeInventory();
			_editor.saveLayout(((Player) e.getWhoClicked()), e.getClickedInventory());
			return;
		}

		if ((e.getSlot() < 9 || e.getSlot() > 17))
		{
			e.setCancelled(true);
			return;
		}

		if (e.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE))
		{
			e.setCancelled(true);
			return;
		}

		if (!(e.getAction().equals(InventoryAction.PICKUP_ONE) || e.getAction().equals(InventoryAction.PLACE_ONE))){

			if (!(e.getSlot() > 8 && e.getSlot() < 18))
			{
				e.setCancelled(true);
				return;
			}

			return;
		}

//		if (e.getAction().equals(InventoryAction.PLACE_ALL)
//				|| e.getAction().equals(InventoryAction.PLACE_ONE)
//				|| e.getAction().equals(InventoryAction.PLACE_SOME))
//		{
//			if (!(e.getSlot() > 8 && e.getSlot() < 18))
//			{
//				e.setCancelled(true);
//				return;
//			}
//		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDrop(PlayerDropItemEvent e){
		if (e.getPlayer().getOpenInventory() == null)
			return;

		if (e.getPlayer().getOpenInventory().getTopInventory() == null)
			return;

		if (!e.getPlayer().getOpenInventory().getTopInventory().getName().equals("Hotbar Editor"))
			return;

		if (!e.isCancelled())
			return;

		e.setCancelled(false);
		Item i = e.getItemDrop();
		Inventory inv = e.getPlayer().getOpenInventory().getTopInventory();

		if (i.getItemStack().getType().equals(Material.EMERALD_BLOCK))
			inv.setItem(30, new ItemBuilder(Material.EMERALD_BLOCK).setTitle(C.cGreen + C.Bold + "Save").setLore(C.cGray + "Click to save layout.").build());
		else if (i.getItemStack().getType().equals(Material.REDSTONE_BLOCK))
			inv.setItem(32, new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cRed + C.Bold + "Cancel").setLore(C.cGray + "Click to cancel layout.").build());
		else
			inv.addItem(i.getItemStack());

		e.getPlayer().updateInventory();
		e.getPlayer().setItemOnCursor(null);

		i.remove();
	}
	
	@EventHandler
	public void updateInv(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		for (Player player : UtilServer.GetPlayers())
		{
			if (player.getOpenInventory() == null)
				continue;
			
			if (player.getOpenInventory().getTopInventory() == null)
				continue;
		
			if (!player.getOpenInventory().getTopInventory().getName().equals("Hotbar Editor"))
				continue;
			
			for (Material mat : new Material[]{Material.DIAMOND_SWORD, Material.BOW, Material.FISHING_ROD, Material.ARROW})
			{	
				boolean founditem = false;
				for (ItemStack item : player.getOpenInventory().getTopInventory().getContents())
				{
					if (item != null && item.getType() == mat)
					{
						founditem = true;
					}
				}
				if (player.getItemOnCursor() != null)
				{
					if (player.getItemOnCursor().getType() == mat)
						founditem = true;
				}
					
				if (!founditem)
				{
					player.getOpenInventory().getTopInventory().addItem(new ItemStack(mat));
				}
				
				
				if (UtilInv.contains(player, null, mat, (byte) 0, 1, false, true, false))
				{
					UtilInv.removeAll(player, mat, (byte) 0);
				}
			}
		}
	}
}
