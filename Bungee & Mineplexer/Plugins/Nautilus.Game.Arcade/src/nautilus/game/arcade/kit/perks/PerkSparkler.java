package nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkSparkler extends Perk
{
	public static class ThrowSparklerEvent extends PlayerEvent
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		private final Item _sparkler;

		public ThrowSparklerEvent(Player who, Item sparkler)
		{
			super(who);

			_sparkler = sparkler;
		}

		public Item getSparkler()
		{
			return _sparkler;
		}
	}

	private HashSet<Item> _items = new HashSet<Item>();

	private int _spawnRate;
	private int _max;

	public PerkSparkler(int spawnRate, int max)
	{
		super("", new String[]
				{
						C.cGray + "Receive 1 Sparkler every " + spawnRate + " seconds. Maximum of " + max + ".",
						C.cYellow + "Click" + C.cGray + " with Sparkler to " + C.cGreen + "Throw Sparkler"
				});

		_spawnRate = spawnRate;
		_max = max;
	}

	public void Apply(Player player)
	{
		Recharge.Instance.use(player, GetName(), _spawnRate * 1000, false, false);
	}

	@EventHandler
	public void SparklerSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				continue;

			if (!Manager.GetGame().IsAlive(cur))
				continue;

			if (!Recharge.Instance.use(cur, GetName(), _spawnRate * 1000, false, false))
				continue;

			if (UtilInv.contains(cur, Material.EMERALD, (byte) 0, _max))
				continue;

			//Add
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, F.item("Throwing Sparkler")));

			cur.playSound(cur.getLocation(), Sound.ITEM_PICKUP, 2f, 1f);
		}
	}

	@EventHandler
	public void SparklerDrop(PlayerDropItemEvent event)
	{
		if (!UtilInv.IsItem(event.getItemDrop().getItemStack(), Material.EMERALD, (byte) 0))
			return;

		//Cancel
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot drop " + F.item("Throwing Sparkler") + "."));
	}

	@EventHandler
	public void SparklerDeathRemove(PlayerDeathEvent event)
	{
		HashSet<org.bukkit.inventory.ItemStack> remove = new HashSet<org.bukkit.inventory.ItemStack>();

		for (org.bukkit.inventory.ItemStack item : event.getDrops())
			if (UtilInv.IsItem(item, Material.EMERALD, (byte) 0))
				remove.add(item);

		for (org.bukkit.inventory.ItemStack item : remove)
			event.getDrops().remove(item);
	}

	@EventHandler
	public void SparklerInvClick(InventoryClickEvent event)
	{
		UtilInv.DisallowMovementOf(event, "Throwing Sparkler", Material.EMERALD, (byte) 0, true);
	}

	@EventHandler
	public void SparklerThrow(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK &&
				event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_AIR)
			return;

		Player player = event.getPlayer();

		if (!UtilInv.IsItem(player.getItemInHand(), Material.EMERALD, (byte) 0))
			return;

		if (!Kit.HasKit(player))
			return;

		event.setCancelled(true);

		UtilInv.remove(player, Material.EMERALD, (byte) 0, 1);
		UtilInv.Update(player);

		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, F.item("Throwing Sparkler")));

		item.setPickupDelay(2000);

		_items.add(item);

		UtilAction.velocity(item, player.getLocation().getDirection(), 0.8, false, 0, 0.1, 10, false);

		Bukkit.getPluginManager().callEvent(new ThrowSparklerEvent(player, item));
	}

	@EventHandler
	public void Sparkle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<Item> itemIterator = _items.iterator();

		while (itemIterator.hasNext())
		{
			Item item = itemIterator.next();

			if (!item.isValid() || item.getTicksLived() > 100)
			{
				item.remove();
				itemIterator.remove();
				continue;
			}

			UtilFirework.playFirework(item.getLocation(), Type.BURST, Color.YELLOW, false, false);
		}
	}

	public HashSet<Item> GetItems()
	{
		return _items;
	}
}
