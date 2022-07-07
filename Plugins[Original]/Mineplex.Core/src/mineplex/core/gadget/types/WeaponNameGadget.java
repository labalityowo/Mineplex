package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.arcadeevents.CoreGameStartEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.weaponname.WeaponNameType;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WeaponNameGadget extends Gadget
{

	private final WeaponNameType _weaponNameType;

	public WeaponNameGadget(GadgetManager manager, WeaponNameType weaponNameType)
	{
		super(manager, GadgetType.WEAPON_NAME, weaponNameType.getName(), new String[]
				{
						C.cGray + "Changes the name of your",
						ItemStackFactory.Instance.GetName(weaponNameType.getWeaponType().getMaterial(), (byte) 0, true) + C.cGray + " to " + F.item(weaponNameType.getDisplay()) + "."
				}, weaponNameType.getCost(), weaponNameType.getMaterial(), weaponNameType.getMaterialData());

		_weaponNameType = weaponNameType;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		for (Gadget gadget : Manager.getGadgets(GadgetType.WEAPON_NAME))
		{
			if (((WeaponNameGadget) gadget).getWeaponNameType().getWeaponType() == _weaponNameType.getWeaponType())
			{
				gadget.disable(player);
			}
		}

		_active.add(player);

		if (message)
		{
			player.sendMessage(F.main(Manager.getName(), "You enabled " + F.name(getName()) + "."));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void inventroyClick(InventoryClickEvent event)
	{
		if (event.isCancelled() || event.getClickedInventory() == null)
		{
			return;
		}

		Player player = (Player) event.getWhoClicked();

		if (!isEnabled(player))
		{
			return;
		}

		InventoryAction action = event.getAction();
		ItemStack itemStack = event.getCursor();
		boolean playerInv = event.getClickedInventory().equals(player.getInventory());

		if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
		{
			playerInv = !playerInv;
			itemStack = event.getCurrentItem();
		}

		if (isItem(itemStack) && !playerInv)
		{
			setItemName(itemStack, null);
		}
		else if (isItemBase(itemStack) && playerInv)
		{
			setItemName(itemStack);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerDropItem(PlayerDropItemEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();

		if (!isEnabled(player))
		{
			return;
		}

		ItemStack itemStack = event.getItemDrop().getItemStack();

		if (isItem(itemStack))
		{
			setItemName(itemStack, null);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerPickupItem(PlayerPickupItemEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem().getItemStack();
		boolean active = isActive(player);

		if (isItemBase(itemStack) && active)
		{
			setItemName(itemStack);
		}
		else if (isItem(itemStack) && !active)
		{
			setItemName(itemStack, null);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		if (!isEnabled(player))
		{
			return;
		}

		event.getDrops().forEach(itemStack ->
		{
			if (isItem(itemStack))
			{
				setItemName(itemStack, null);
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void updateInventoryFallback(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWER)
		{
			return;
		}

		validateInventories();
	}

	@EventHandler
	public void gameStart(CoreGameStartEvent event)
	{
		Manager.runSyncLater(this::validateInventories, 10);
	}

	private boolean isEnabled(Player player)
	{
		return Manager.showWeaponNames() && isActive(player);
	}

	private boolean isItemBase(ItemStack itemStack)
	{
		return itemStack != null && !isItem(itemStack) && itemStack.getType() == _weaponNameType.getWeaponType().getMaterial() && (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName());
	}

	private boolean isItem(ItemStack itemStack)
	{
		if (itemStack == null || itemStack.getType() != _weaponNameType.getWeaponType().getMaterial() || !itemStack.hasItemMeta())
		{
			return false;
		}

		String name = itemStack.getItemMeta().getDisplayName();

		return name != null && name.contains(_weaponNameType.getDisplay());
	}

	private void setItemName(ItemStack itemStack)
	{
		setItemName(itemStack, C.mItem + _weaponNameType.getDisplay());
	}

	private void setItemName(ItemStack itemStack, String name)
	{
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(name);
		itemStack.setItemMeta(itemMeta);
	}

	private void validateInventories()
	{
		if (!Manager.showWeaponNames())
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			boolean active = isActive(player);

			for (ItemStack itemStack : player.getInventory().getContents())
			{
				if (isItem(itemStack) && !active)
				{
					setItemName(itemStack, null);
				}
				else if (isItemBase(itemStack) && active)
				{
					setItemName(itemStack);
				}
			}
		}
	}

	public WeaponNameType getWeaponNameType()
	{
		return _weaponNameType;
	}

	public enum WeaponType
	{

		WOOD_SWORD(0, Material.WOOD_SWORD),
		STONE_SWORD(1, Material.STONE_SWORD),
		GOLD_SWORD(2, Material.GOLD_SWORD),
		IRON_SWORD(3, Material.IRON_SWORD),
		DIAMOND_SWORD(4, Material.DIAMOND_SWORD),;

		private final int _id;
		private final Material _material;

		WeaponType(int id, Material material)
		{
			_id = id;
			_material = material;
		}

		public int getId()
		{
			return _id;
		}

		public Material getMaterial()
		{
			return _material;
		}
	}

}
