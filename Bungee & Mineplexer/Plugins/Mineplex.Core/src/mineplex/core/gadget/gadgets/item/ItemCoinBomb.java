package mineplex.core.gadget.gadgets.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemCoinBomb extends ItemGadget
{
	private HashMap<Item, Long> _active = new HashMap<Item, Long>();
	private HashSet<Item> _coins = new HashSet<Item>();

	public ItemCoinBomb(GadgetManager manager)
	{
		super(manager, "Treasure Party Bomb",
				UtilText.splitLineToArray(C.cWhite + "It's party time! You'll be everyone's favorite player when you use one of these!", LineFormat.LORE),
				-1,
				Material.PRISMARINE, (byte) 0,
				30000, new Ammo("Treasure Party Bomb", "1 Coin Party Bomb", Material.PRISMARINE, (byte) 0, new String[]{C.cWhite + "1 Treasure Party Bomb"}, 2000, 1));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), new ItemStack(Material.PRISMARINE));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1, false, 0, 0.2, 1, false);
		_active.put(item, System.currentTimeMillis());

		//Inform
		for (Player other : UtilServer.getPlayers())
			UtilPlayer.message(other, C.cAqua + C.Bold + player.getName() + C.cWhite + C.Bold + " has thrown a " + C.cAqua + C.Bold + "Treasure Party Bomb" + C.cWhite + C.Bold + "!");
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Item> itemIterator = _active.keySet().iterator();

		while (itemIterator.hasNext())
		{
			Item item = itemIterator.next();
			long time = _active.get(item);

			if (UtilTime.elapsed(time, 3000))
			{
				if (Math.random() > 0.80)
					UtilFirework.playFirework(item.getLocation(), FireworkEffect.builder().flicker(false).withColor(Color.AQUA).with(Type.BURST).trail(false).build());
				else
					item.getWorld().playSound(item.getLocation(), Sound.FIREWORK_LAUNCH, 1f, 1f);

				Item coin = item.getWorld().dropItem(item.getLocation().add(0, 1, 0), new ItemStack(Material.PRISMARINE_SHARD));

				//Velocity
				long passed = System.currentTimeMillis() - time;
				Vector vel = new Vector(Math.sin(passed / 300d), 0, Math.cos(passed / 300d));

				UtilAction.velocity(coin, vel, Math.abs(Math.sin(passed / 3000d)), false, 0, 0.2 + Math.abs(Math.cos(passed / 3000d)) * 0.8, 1, false);

				coin.setPickupDelay(40);

				_coins.add(coin);
			}

			if (UtilTime.elapsed(time, 23000))
			{
				item.remove();
				itemIterator.remove();
			}
		}
	}

	@EventHandler
	public void Pickup(PlayerPickupItemEvent event)
	{
		if (_active.keySet().contains(event.getItem()))
		{
			event.setCancelled(true);
		}
		else if (_coins.contains(event.getItem()))
		{
			event.setCancelled(true);
			event.getItem().remove();

			Manager.getDonationManager().rewardCurrency(GlobalCurrency.TREASURE_SHARD, event.getPlayer(), getName() + " Pickup", 4);

			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 2f);

			//UtilPlayer.message(event.getPlayer(), C.cGreen + C.Bold + "+4 Gems");
		}
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<Item> coinIterator = _coins.iterator();

		while (coinIterator.hasNext())
		{
			Item coin = coinIterator.next();

			if (!coin.isValid() || coin.getTicksLived() > 1200)
			{
				coin.remove();
				coinIterator.remove();
			}
		}
	}
}
