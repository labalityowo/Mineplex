package mineplex.core.gadget.gadgets.death;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class DeathSpring extends DeathEffectGadget
{

	private List<Item> _items = new ArrayList<>();

	public DeathSpring(GadgetManager manager)
	{
		super(manager, "Funeral Bouquet",
				UtilText.splitLineToArray(C.cGray + "Leave a rose to pay respects", LineFormat.LORE),
				-19,
				Material.YELLOW_FLOWER, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);

		final Location location = event.getLocation();
		while (location.getBlock().getType() != Material.AIR
				&& location.clone().add(0, 1, 0).getBlock().getType() != Material.AIR)
		{
			location.add(0, 1, 0);
		}

		location.getBlock().setType(Material.DOUBLE_PLANT);
		location.getBlock().setData((byte) 4);

		Bukkit.getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				location.getBlock().setType(Material.AIR);

				// Creates red rose explosion
				for (int i = 50; i < 60; i++)
				{
					Item rose = location.getWorld().dropItem(location.add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.RED_ROSE, (byte) 0, 1, " " + i));
					_items.add(rose);

					Vector vel = new Vector(Math.sin(i * 9/5d), 0, Math.cos(i * 9/5d));
					UtilAction.velocity(rose, vel, Math.abs(Math.sin(i * 12/3000d)), false, 0, 0.2 + Math.abs(Math.cos(i * 12/3000d))*0.6, 1, false);

				}
			}
		}, 60L);
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
