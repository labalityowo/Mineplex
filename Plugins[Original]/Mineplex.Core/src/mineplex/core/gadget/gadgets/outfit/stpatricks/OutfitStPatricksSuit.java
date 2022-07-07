package mineplex.core.gadget.gadgets.outfit.stpatricks;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class OutfitStPatricksSuit extends OutfitGadget
{

	private HashSet<Item> _items = new HashSet<>();

	public OutfitStPatricksSuit(GadgetManager manager, String name, int cost, OutfitGadget.ArmorSlot slot, Material mat, byte data, String... altNames)
	{
		super(manager, name, UtilText.splitLinesToArray(new String[]{C.cGray + "Wherever a Leprechaun goes he leaves behind a trail of gold and a hint of spring."}, LineFormat.LORE), cost, slot, mat, data, altNames);
		setColor(Color.fromRGB(0, 153, 0));
		ItemStack displayItem = new ItemStack(mat, 1, data);
		if (displayItem.getItemMeta() instanceof LeatherArmorMeta)
		{
			LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) displayItem.getItemMeta();
			leatherArmorMeta.setColor(Color.fromRGB(0, 153, 0));
			displayItem.setItemMeta(leatherArmorMeta);
		}
		setDisplayItem(displayItem);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		// Prevents event running 4 times
		if (getSlot() != ArmorSlot.HELMET)
			return;

		if (event.getType() == UpdateType.TICK)
			cleanItems();

		if (event.getType() == UpdateType.FASTER)
		{
			for (Player player : getActive())
			{
				if (setActive(player))
				{
					ItemStack itemStack = new ItemStack(Material.GOLD_INGOT);
					ItemMeta itemMeta = itemStack.getItemMeta();
					itemMeta.setDisplayName(player.getName() + "DROPPED" + System.currentTimeMillis() + UtilMath.random.nextInt());
					itemStack.setItemMeta(itemMeta);
					Item gold = player.getWorld().dropItem(player.getLocation().add(0.5, 1.5, 0.5), itemStack);
					_items.add(gold);
					gold.setVelocity(new Vector((Math.random() - 0.5) * 0.3, Math.random() - 0.4, (Math.random() - 0.5) * 0.3));
				}
			}
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event)
	{
		// Prevents event running 4 times
		if (getSlot() != ArmorSlot.HELMET)
			return;

		if (_items.contains(event.getItem()))
		{
			event.setCancelled(true);
		}
	}

	private boolean setActive(Player player)
	{
		return getSet() != null && getSet().isActive(player);
	}

	public void cleanItems()
	{
		Iterator<Item> it = _items.iterator();
		while (it.hasNext())
		{
			Item item = it.next();
			if (item.getTicksLived() >= 20)
			{
				item.remove();
				it.remove();
			}
		}
	}

}
