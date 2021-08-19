package mineplex.core.visibility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

@ReflectivelyCreateMiniPlugin
public class VisibilityManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		VIS_DEBUG,
	}
	
	private static final int VIEW_DISTANCE = Bukkit.getViewDistance() * 16;
	
	private final Map<Player, Map<Player, Set<String>>> _visibility = new HashMap<>();
	
	private VisibilityManager()
	{
		super("Visibility Manager");
		
		addCommand(new CommandBase<VisibilityManager>(this, Perm.VIS_DEBUG, "visdebug")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				Player target = caller;
				if (args.length > 0)
				{
					if (Bukkit.getPlayer(args[0]) != null)
					{
						target = Bukkit.getPlayer(args[0]);
					}
				}
				
				caller.sendMessage(target.getName() + " (Bukkit):");
				caller.sendMessage(caller.getName() + " Observing " + target.getName() + ": " + caller.canSee(target));
				caller.sendMessage(target.getName() + " Observing " + caller.getName() + ": " + target.canSee(caller));
				caller.sendMessage(target.getName() + " (Mineplex):");
				caller.sendMessage(caller.getName() + " Observing " + target.getName() + ": " + canSee(caller, target));
				caller.sendMessage(target.getName() + " Observing " + caller.getName() + ": " + canSee(target, caller));
				caller.sendMessage(caller.getName() + " Data (Mineplex):");
				_visibility.get(caller).entrySet().forEach(entry ->
				{
					caller.sendMessage("- " + entry.getKey().getName() + ":");
					entry.getValue().forEach(reason ->
					{
						caller.sendMessage(" - " + reason);
					});
				});
				if (caller.getEntityId() != target.getEntityId())
				{
					caller.sendMessage(target.getName() + " Data (Mineplex):");
					_visibility.get(target).entrySet().forEach(entry ->
					{
						caller.sendMessage("- " + entry.getKey().getName() + ":");
						entry.getValue().forEach(reason ->
						{
							caller.sendMessage(" - " + reason);
						});
					});
				}
			}
		});
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.VIS_DEBUG, true, true);
		PermissionGroup.QA.setPermission(Perm.VIS_DEBUG, true, true);
	}
	
	public boolean canSee(Player viewer, Player target)
	{
		return _visibility.get(viewer).getOrDefault(target, new HashSet<>()).isEmpty();
	}
	
	public void refreshVisibility(Player viewer, Player target)
	{
		if (viewer == null || target == null)
		{
			return;
		}
		if (viewer.getEntityId() == target.getEntityId())
		{
			return;
		}
		if (canSee(viewer, target))
		{
			viewer.showPlayer(target);
		}
		else
		{
			viewer.hidePlayer(target);
		}
	}
	
	public void hidePlayer(Player viewer, Player target, String reason)
	{
		if (viewer == null || target == null || reason == null)
		{
			return;
		}
		if (viewer.getEntityId() == target.getEntityId())
		{
			return;
		}
		Set<String> reasons = _visibility.get(viewer).computeIfAbsent(target, (p) -> new HashSet<>());
		if (reasons.contains(reason))
		{
			return;
		}
		reasons.add(reason);
		refreshVisibility(viewer, target);
	}
	
	public void showPlayer(Player viewer, Player target, String reason)
	{
		if (viewer == null || target == null || reason == null)
		{
			return;
		}
		if (viewer.getEntityId() == target.getEntityId())
		{
			return;
		}
		Set<String> reasons = _visibility.get(viewer).get(target);
		if (reasons == null)
		{
			return;
		}
		boolean modified = reasons.remove(reason);
		if (reasons.isEmpty())
		{
			_visibility.get(viewer).remove(target);
		}
		if (modified)
		{
			refreshVisibility(viewer, target);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event)
	{
		_visibility.put(event.getPlayer(), new HashMap<>());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event)
	{
		_visibility.remove(event.getPlayer());
		_visibility.values().forEach(v -> v.remove(event.getPlayer()));
	}
	
	/*@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC_05)
		{
			return;
		}
		Bukkit.getOnlinePlayers().forEach(player ->
		{
			player.getNearbyEntities(VIEW_DISTANCE, VIEW_DISTANCE, VIEW_DISTANCE).forEach(entity ->
			{
				if (entity instanceof Player)
				{
					refreshVisibility(player, (Player)entity);
				}
			});
		});
	}*/
}