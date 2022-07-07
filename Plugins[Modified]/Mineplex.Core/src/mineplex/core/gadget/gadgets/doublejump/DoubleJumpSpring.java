package mineplex.core.gadget.gadgets.doublejump;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class DoubleJumpSpring extends DoubleJumpEffectGadget
{

	private List<Item> _items = new ArrayList<>();

	public DoubleJumpSpring(GadgetManager manager)
	{
		// TODO NAME LORE ICON
		super(manager, "Flower Power", UtilText.splitLineToArray(C.cGray + "Be like a bouncing bee and pollinate everywhere you go!", LineFormat.LORE), -19,
				Material.YELLOW_FLOWER, (byte) 0);
	}

	@Override
	public void doEffect(Player player)
	{
		for (int i = 50; i < 60; i++)
		{
			Item sunflower = player.getWorld().dropItem(player.getLocation().add(0.0, 0.2, 0.0), ItemStackFactory.Instance.CreateStack(Material.DOUBLE_PLANT, (byte) 0, 1, " " + i));
			_items.add(sunflower);

			Vector vel = new Vector(Math.sin(i * 9/5d), 0, Math.cos(i * 9/5d));
			UtilAction.velocity(sunflower, vel, Math.abs(Math.sin(i * 12/3000d)), false, 0, 0.2 + Math.abs(Math.cos(i * 12/3000d))*0.6, 1, false);

		}
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
