package mineplex.core.gadget.gadgets.arrowtrail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ArrowTrailSpring extends ArrowEffectGadget
{

	private List<Item> _items = new ArrayList<>();

	public ArrowTrailSpring(GadgetManager manager)
	{
		super(manager, "Flower Arrows", UtilText.splitLineToArray(C.cGray + "Send the power of Spring flying at your foes!", LineFormat.LORE),
				-19, Material.YELLOW_FLOWER, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		Item sunflower = arrow.getWorld().dropItem(arrow.getLocation(), new ItemStack(Material.DOUBLE_PLANT));
		_items.add(sunflower);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{

	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event)
	{
		if (_items.contains(event.getItem()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void removeFlowers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Item> iterator = _items.iterator();
		while (iterator.hasNext())
		{
			Item item = iterator.next();
			if (item.getTicksLived() >= 20)
			{
				item.remove();
				iterator.remove();
			}
		}
	}

}
