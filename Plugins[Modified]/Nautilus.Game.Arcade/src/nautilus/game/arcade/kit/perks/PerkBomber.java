package nautilus.game.arcade.kit.perks;

import mineplex.core.common.util.*;
import mineplex.core.itemstack.*;
import mineplex.core.recharge.*;
import mineplex.core.updater.*;
import mineplex.core.updater.event.*;
import nautilus.game.arcade.kit.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PerkBomber extends Perk
{
	public static class BomberExplodeDiamondBlock extends PlayerEvent
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

		private final Block _block;
		private boolean _spawnDrop;

		public BomberExplodeDiamondBlock(Player who, Block block)
		{
			super(who);

			_block = block;
			_spawnDrop = true;
		}

		public Block getBlock()
		{
			return _block;
		}

		public void setSpawnDrop(boolean b)
		{
			_spawnDrop = b;
		}

		public boolean shouldSpawnDrop()
		{
			return _spawnDrop;
		}
	}

	private static final String THROWING_TNT_DISPLAY_NAME = F.item("Throwing TNT");

	// Maps how many TNT each player has gotten, regardless of where that TNT is
	private Map<Player, AtomicInteger> _tntCount = new HashMap<>();
	private Map<Entity, Player> _tntMap = new HashMap<Entity, Player>();

	private int _spawnRate;
	private int _max;
	private int _fuse;

	public PerkBomber(int spawnRate, int max, int fuse)
	{
		super("Bomber", new String[]
				{
						C.cGray + "Receive 1 TNT every " + spawnRate + " seconds. Maximum of " + max + ".",
						C.cYellow + "Click" + C.cGray + " with TNT to " + C.cGreen + "Throw TNT"
				});

		_spawnRate = spawnRate;
		_max = max;
		_fuse = fuse;
	}

	public void Apply(Player player)
	{
		Recharge.Instance.use(player, GetName(), _spawnRate * 1000, false, false);
	}

	@EventHandler
	public void TNTSpawn(UpdateEvent event)
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

			if (_tntCount.get(cur).get() >= _max)
				continue;

			//Add
			ItemStack tnt = new ItemBuilder(Material.TNT).addLore(generateLore(cur)).setTitle(THROWING_TNT_DISPLAY_NAME).build();
			cur.getInventory().addItem(tnt);

			cur.playSound(cur.getLocation(), Sound.ITEM_PICKUP, 2f, 1f);

			_tntCount.get(cur).incrementAndGet();
		}
	}

	@EventHandler
	public void TNTDrop(PlayerDropItemEvent event)
	{
		if (event.isCancelled())
			return;

		if (!isThrowingTnt(event.getItemDrop().getItemStack()))
			return;

		//Cancel
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot drop " + F.item("Throwing TNT") + "."));
	}

	@EventHandler (ignoreCancelled = true)
	public void on(BlockPlaceEvent event)
	{
		if (!isThrowingTnt(event.getItemInHand()))
			return;
		event.setCancelled(true);
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot place " + F.item("Throwing TNT") + "."));
	}

	@EventHandler
	public void TNTDeathRemove(PlayerDeathEvent event)
	{
		HashSet<org.bukkit.inventory.ItemStack> remove = new HashSet<org.bukkit.inventory.ItemStack>();

		for (org.bukkit.inventory.ItemStack item : event.getDrops())
			if (isThrowingTnt(item))
				remove.add(item);

		int oldAmount = _tntCount.get(event.getEntity()).get();
		if (oldAmount < remove.size())
		{
			// fixme If you die with someone else's tnt, then this will occur
			System.out.println("Strange.... expected " + oldAmount + " of tnt for " + event.getEntity().getName() + " but removed " + remove.size() + " instead");
			_tntCount.get(event.getEntity()).set(0);
		}
		else
		{
			_tntCount.get(event.getEntity()).set(oldAmount - remove.size());
		}

		for (org.bukkit.inventory.ItemStack item : remove)
			event.getDrops().remove(item);
	}

	@EventHandler
	public void on(PlayerQuitEvent event)
	{
		_tntCount.remove(event.getPlayer());
	}

	@Override
	public void registeredEvents()
	{
		_tntCount.clear();
		for (Player player : UtilServer.getPlayersCollection())
		{
			_tntCount.put(player, new AtomicInteger());
		}
	}

	@Override
	public void unregisteredEvents()
	{
		_tntCount.clear();
	}

	@EventHandler
	public void TNTThrow(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, UtilEvent.ActionType.L) && !UtilEvent.isAction(event, UtilEvent.ActionType.R))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!isThrowingTnt(event.getItem()))
			return;

		ItemMeta meta = player.getItemInHand().getItemMeta();
		List<String> lore = meta.getLore();
		String[] ownerInfo = ChatColor.stripColor(lore.get(1)).split(":");
		ownerInfo[1] = ownerInfo[1].trim();
		if (!player.getName().equals(ownerInfo[1]))
		{
			UtilPlayer.message(player, F.main(GetName(), "You cannot throw TNT owned by " + C.cGreen + ownerInfo[1]));
			return;
		}

		event.setCancelled(true);

		if (!Manager.GetGame().CanThrowTNT(player.getLocation()))
		{
			//Inform
			UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot use " + F.item("Throwing TNT") + " here."));
			return;
		}

		if (event.getItem().getAmount() > 1)
		{
			event.getItem().setAmount(event.getItem().getAmount() - 1);
		}
		else
		{
			player.getInventory().remove(event.getItem());
		}
		UtilInv.Update(player);

		TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), TNTPrimed.class);

		if (_fuse != -1)
			tnt.setFuseTicks(_fuse);

		UtilAction.velocity(tnt, player.getLocation().getDirection(), 0.5, false, 0, 0.1, 10, false);

		tnt.setMetadata("owner", new FixedMetadataValue(Manager.getPlugin(), player.getUniqueId()));

		_tntMap.put(tnt, player);
		_tntCount.get(player).decrementAndGet();
	}

	@EventHandler
	public void ExplosionPrime(ExplosionPrimeEvent event)
	{
		Player player = _tntMap.get(event.getEntity());
		if (player != null)
		{
			for (Player other : UtilPlayer.getNearby(event.getEntity().getLocation(), 14))
			{
				Manager.GetCondition().Factory().Explosion("Throwing TNT", other, player, 50, 0.1, false, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		Player player = _tntMap.remove(event.getEntity());

		if (player != null)
		{
			for (Iterator<Block> it = event.blockList().iterator(); it.hasNext(); )
			{
				Block block = it.next();

				if (block.getType() == Material.DIAMOND_ORE)
				{
					BomberExplodeDiamondBlock explodeEvent = new BomberExplodeDiamondBlock(player, block);
					UtilServer.CallEvent(explodeEvent);

					if (explodeEvent.shouldSpawnDrop())
					{
						block.breakNaturally();
					}
					it.remove();
				}
			}
		}
	}

	private boolean isThrowingTnt(ItemStack in)
	{
		if (in == null)
		{
			return false;
		}
		ItemMeta meta = in.getItemMeta();
		if (meta == null)
		{
			return false;
		}
		if (!THROWING_TNT_DISPLAY_NAME.equals(meta.getDisplayName()))
		{
			return false;
		}
		List<String> lore = meta.getLore();
		if (lore.size() != 2)
		{
			return false;
		}
		if (!lore.get(1).contains("Owned by:"))
		{
			return false;
		}
		return true;
	}

	private String[] generateLore(Player input)
	{
		return new String[]{
				" ",
				C.cYellow + "Owned by: " + C.Reset + C.cGreen + input.getName()
		};
	}
}
