package mineplex.core.anvilMenu;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

/**
 * A utility class for creating simple and easy Anvil GUI's that require actions based on a users input.
 */
public abstract class PlayerInputActionMenu implements Listener
{

	protected MiniPlugin _plugin;
	protected Player _player;
	protected Inventory _currentInventory;
	protected String _itemName = "";
	protected boolean _searching;

	public PlayerInputActionMenu(MiniPlugin plugin, Player player)
	{
		_player = player;
		_plugin = plugin;
		player.closeInventory();
		_plugin.registerEvents(this);
	}

	public abstract void inputReceived(String name);

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (event.getPlayer() == _player)
		{
			unregisterListener();
		}
	}

	public void unregisterListener()
	{
		if(_currentInventory != null)
		{
			_currentInventory.clear();
		}
		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (event.getPlayer() == _player)
		{
			unregisterListener();
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (event.getRawSlot() < 3)
		{
			event.setCancelled(true);

			if (event.getRawSlot() == 2)
			{
				if (_itemName.length() > 1 && !_searching)
				{
					_searching = true;
					inputReceived(_itemName);
				} else
				{
					_player.playSound(_player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
				}
			}
		} else if (event.isShiftClick())
		{
			event.setCancelled(true);
		}
	}

	public void openInventory()
	{

		EntityPlayer p = ((CraftPlayer) _player).getHandle();

		AnvilContainer container = new AnvilContainer(p);
		int c = p.nextContainerCounter();

		PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(c, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name", new Object[0]));

		UtilPlayer.sendPacket(_player, packet);

		// Set their active container to the container
		p.activeContainer = container;

		// Set their active container window id to that counter stuff
		p.activeContainer.windowId = c;

		// Add the slot listener
		p.activeContainer.addSlotListener(p); // Set the items to the items from the inventory given
		_currentInventory = container.getBukkitView().getTopInventory();

		_currentInventory.setItem(0, new ItemBuilder(Material.PAPER).setRawTitle("Input Text...").build());
		_currentInventory.setItem(2, new ItemBuilder(Material.PAPER).setRawTitle("Search").build());
	}

	private class AnvilContainer extends ContainerAnvil
	{
		private String n;

		public AnvilContainer(EntityHuman entity)
		{
			super(entity.inventory, entity.world, new BlockPosition(0, 0, 0), entity);
		}

		@Override
		public boolean a(EntityHuman entityhuman)
		{
			return true;
		}

		@Override
		public void a(String origString)
		{
			n = origString;
			_itemName = origString;

			if (getSlot(2).hasItem())
			{
				net.minecraft.server.v1_8_R3.ItemStack itemstack = getSlot(2).getItem();

				if (StringUtils.isBlank(origString))
				{
					itemstack.r();
				} else
				{
					itemstack.c(this.n);
				}
			}

			e();
		}

	}

}
