package mineplex.gemhunters.economy;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.StatsManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.economy.command.CashOutItemCommand;
import mineplex.gemhunters.economy.command.ResetCooldownCommand;
import mineplex.gemhunters.economy.event.PlayerCashOutCompleteEvent;
import mineplex.gemhunters.spawn.event.PlayerTeleportIntoMapEvent;

@ReflectivelyCreateMiniPlugin
public class CashOutModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		CASH_OUT_ITEM_COMMAND,
		RESET_COOLDOWN_COMMAND,
	}

	private static final DecimalFormat ARMOUR_STAND_FORMAT = new DecimalFormat("0.0");
	public static final ItemStack CASH_OUT_ITEM = new ItemBuilder(Material.EMERALD).setTitle(C.cGreen + "Cash Out").addLore("", "Click to begin the process to cash out.", "Cashing out gives you your gems, shards,", "chests and any particles you have.", "However you will lose all your current loot!").build();

	private static final int CASH_OUT_SECONDS = 10;   
	private static final int CASH_OUT_COOLDOWN = 10000;
	private static final int CASH_OUT_MAX_MOVE_DISTANCE_SQUARED = 4;

	private final DonationManager _donation;
	private final StatsManager _stats;

	private final Map<UUID, CashOutSession> _sessions;
	private final Set<UUID> _aboutToCashOut;

	public CashOutModule()
	{
		super("Cash Out");

		_donation = require(DonationManager.class);
		_stats = require(StatsManager.class);

		_sessions = new HashMap<>();
		_aboutToCashOut = new HashSet<>();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.CASH_OUT_ITEM_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.RESET_COOLDOWN_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new CashOutItemCommand(this));
		addCommand(new ResetCooldownCommand(this));
	}
	
	@EventHandler
	public void teleportIn(PlayerTeleportIntoMapEvent event)
	{
		if (event.isCancelled() || event.getPlayer().getInventory().contains(CASH_OUT_ITEM))
		{
			return;
		}
		
		event.getPlayer().getInventory().setItem(7, CASH_OUT_ITEM);
	}
	
	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{ 
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null)
		{
			return;
		}

		if (!itemStack.isSimilar(CASH_OUT_ITEM))
		{
			return;
		}

		attemptCashOut(player);
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().isSimilar(CASH_OUT_ITEM))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<UUID> iterator = _sessions.keySet().iterator();

		while (iterator.hasNext())
		{
			UUID key = iterator.next();
			final Player player = UtilPlayer.searchExact(key);
			CashOutSession session = _sessions.get(key);
			double current = session.getCurrent();
			ArmorStand stand = session.getArmourStand();
			String standName = ARMOUR_STAND_FORMAT.format(current);

			if (player == null)
			{
				session.endSession();
				iterator.remove();
				continue;
			}

			UtilTextMiddle.display(C.cGreen + standName, UtilTextMiddle.progress((float) (1 - current / session.getMax())), 0, 10, 0, player);
			stand.setCustomName(standName + " seconds");
			session.setCurrent(current - 0.05);

			if (session.getCurrent() <= 0)
			{
				PlayerCashOutCompleteEvent completeEvent = new PlayerCashOutCompleteEvent(player);

				UtilServer.CallEvent(completeEvent);

				_aboutToCashOut.add(player.getUniqueId());

				if (completeEvent.getGems() != EconomyModule.GEM_START_COST)
				{
					_stats.incrementStat(player, "Gem Hunters.GemsEarned", completeEvent.getGems());
				}

				_donation.rewardCurrencyUntilSuccess(GlobalCurrency.GEM, player, "Earned", completeEvent.getGems());

				session.endSession();
				iterator.remove();

				player.getInventory().clear();

				if (UtilServer.isTestServer())
				{
					kickPlayer(player);
				}
				else
				{
					Portal.getInstance().sendPlayerToGenericServer(player, GenericServer.HUB, Intent.FORCE_TRANSFER);

					runSyncLater(() ->
					{
						if (player.isOnline())
						{
							kickPlayer(player);
						}
					}, 50);
				}
			}
		}
	}

	@EventHandler
	public void updateMove(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (UUID key : _sessions.keySet())
		{
			Player player = UtilPlayer.searchExact(key);
			CashOutSession session = _sessions.get(key);

			if (session.getLocation().distanceSquared(player.getLocation()) > CASH_OUT_MAX_MOVE_DISTANCE_SQUARED)
			{
				cancelCashOut(player, "You moved!");
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void entityDamage(EntityDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
		
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (isCashingOut(player))
		{
			cancelCashOut(player, "You took damage!");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void entityAttack(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
		
		if (!(event.getDamager() instanceof Player) || event.getEntity() instanceof ArmorStand)
		{
			return;
		}

		Player player = (Player) event.getDamager();

		if (isCashingOut(player))
		{
			cancelCashOut(player, "You attacked a player!");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerQuit(PlayerQuitEvent event)
	{
		_aboutToCashOut.remove(event.getPlayer().getUniqueId());
	}

	public void attemptCashOut(Player player)
	{
		UUID key = player.getUniqueId();

		if (_sessions.containsKey(key))
		{
			player.sendMessage(F.main("Game", "You are already cashing out."));
			return;
		}

		if (!Recharge.Instance.use(player, "Cash Out", CASH_OUT_COOLDOWN, true, false))
		{
			return;
		}

		_sessions.put(key, new CashOutSession(player, CASH_OUT_SECONDS));
	}

	public void cancelCashOut(Player player, String message)
	{
		UUID key = player.getUniqueId();
		CashOutSession session = _sessions.get(key);

		player.sendMessage(F.main("Game", message + " Your cash out has been cancelled."));
		session.endSession();
		_sessions.remove(key);
	}

	public boolean isCashingOut(Player player)
	{
		return getCashOutSession(player) != null;
	}

	public boolean isAboutToCashOut(Player player)
	{
		return _aboutToCashOut.contains(player.getUniqueId());
	}

	public CashOutSession getCashOutSession(Player player)
	{
		for (UUID key : _sessions.keySet())
		{
			if (key.equals(player.getUniqueId()))
			{
				return _sessions.get(key);
			}
		}

		return null;
	}

	private void kickPlayer(Player player)
	{
		player.kickPlayer(C.cGreen + "Imagine you are being sent to the lobby.");
	}
}