package mineplex.game.clans.clans.observer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.observer.command.ObserverCommand;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class ObserverManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		OBSERVE_COMMAND,
	}

	// Used to Cloak Players
	private final ConditionManager _conditionManager;
	private final Map<Player, ObserverData> _observerMap;

	public ObserverManager(JavaPlugin plugin, ConditionManager conditionManager, ClansManager clansManager)
	{
		super("Observer", plugin);

		_conditionManager = conditionManager;
		_observerMap = new HashMap<>();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.TRAINEE.setPermission(Perm.OBSERVE_COMMAND, true, true);
	}

	public void setObserver(Player player)
	{
		ObserverData data = new ObserverData(player);
		((CraftPlayer) player).getHandle().spectating = true;
		_conditionManager.Clean(player);
		_conditionManager.Factory().Cloak("Observer", player, null, Integer.MAX_VALUE, true, true);
		UtilPlayer.clearInventory(player);
		UtilPlayer.clearPotionEffects(player);
		player.setGameMode(GameMode.CREATIVE);
		ClansManager.getInstance().getGearManager().getPlayerGear(player).updateCache(true);
		_observerMap.put(player, data);
	}

	public void removeObserver(Player player)
	{
		ObserverData data = _observerMap.get(player);
		restore(player, data);
		_observerMap.remove(player);
	}

	public boolean isObserver(Player player)
	{
		return _observerMap.containsKey(player);
	}

	private void restore(Player player, ObserverData data)
	{
		data.getSnapshot().applySnapshot(player);
		player.setGameMode(GameMode.SURVIVAL);
		((CraftPlayer) player).getHandle().spectating = false;

		Condition condition = _conditionManager.GetActiveCondition(player, ConditionType.CLOAK);

		if (condition != null)
		{
			condition.Expire();
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (_observerMap.containsKey(event.getDamager()) || _observerMap.containsKey(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event)
	{
		ObserverData data = _observerMap.get(event.getPlayer());

		if (data != null)
		{
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Chest)
			{
				if (!data.getSettings().contains(ObserverSettings.CAN_OPEN_CHESTS))
				{
					notify(event.getPlayer(), "You cannot open chests as an observer");
					event.setCancelled(true);
				}
			}
			else
			{
				if (!data.getSettings().contains(ObserverSettings.CAN_INTERACT))
				{
					notify(event.getPlayer(), "You cannot interact as an observer");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event)
	{
		ObserverData data = _observerMap.get(event.getPlayer());

		if (data != null)
		{
			if (!data.getSettings().contains(ObserverSettings.CAN_BREAK_BLOCKS))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		ObserverData data = _observerMap.get(event.getWhoClicked());

		if (data != null)
		{
			if (!data.getSettings().contains(ObserverSettings.CAN_CLICK_INVENTORY))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onPickup(PlayerPickupItemEvent event)
	{
		ObserverData data = _observerMap.get(event.getPlayer());

		if (data != null)
		{
			if (!data.getSettings().contains(ObserverSettings.CAN_PICKUP_ITEMS))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		if (_observerMap.containsKey(event.getPlayer()))
		{
			restore(event.getPlayer(), _observerMap.get(event.getPlayer()));
			_observerMap.remove(event.getPlayer());
		}
	}

	@Override
	public void disable()
	{
		super.disable();

		for (Map.Entry<Player, ObserverData> entry : _observerMap.entrySet())
		{
			restore(entry.getKey(), entry.getValue());
		}
	}

	public boolean canEnterObserverMode(Player player, boolean notify)
	{
		return true;
	}

	private void notify(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Observer", message));
	}

	@Override
	public void addCommands()
	{
		addCommand(new ObserverCommand(this));
	}
}