package mineplex.clanshub;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.preferences.Preference;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;

/**
 * Manager for Hub Visibility of players
 */
public class HubVisibilityManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		BYPASS_INVISIBILITY,
	}

	public HubManager Manager;

	private HashSet<Player> _hiddenPlayers = new HashSet<Player>();

	public HubVisibilityManager(HubManager manager)
	{
		super("Visibility Manager", manager.getPlugin());

		Manager = manager;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.MOD.setPermission(Perm.BYPASS_INVISIBILITY, true, true);
	}
	
	/**
	 * Force sets a player as hidden
	 * @param player The player to set
	 */
	public void addHiddenPlayer(Player player)
	{
		_hiddenPlayers.add(player);

	}
	
	/**
	 * Force unsets a player as hidden
	 * @param player The player to unset
	 */
	public void removeHiddenPlayer(Player player)
	{
		_hiddenPlayers.remove(player);
	}

	@EventHandler
	public void removeHiddenPlayerOnQuit(PlayerQuitEvent event)
	{
		_hiddenPlayers.remove(event.getPlayer());
	}

	@EventHandler
	public void updateVisibility(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		VisibilityManager vm = Managers.get(VisibilityManager.class);
		
		for (Player player : UtilServer.getPlayers())
		{
			boolean hideMe = UtilMath.offset2d(player.getLocation(), Manager.GetSpawn()) == 0 || 
					(Manager.getPreferences().get(player).isActive(Preference.INVISIBILITY) && Manager.GetClients().Get(player).hasPermission(Preference.INVISIBILITY)) ||
					_hiddenPlayers.contains(player);

			for (Player other : UtilServer.getPlayers())
			{
				boolean localHideMe = hideMe;
				if (player.equals(other))
					continue;

				if (Manager.GetClients().Get(other).hasPermission(Perm.BYPASS_INVISIBILITY))
					localHideMe = false;
				
				if (localHideMe || !Manager.getPreferences().get(other).isActive(Preference.SHOW_PLAYERS))
				{
					vm.hidePlayer(other, player, "Hub Visibility Manager");
				}
				else
				{
					vm.showPlayer(other, player, "Hub Visibility Manager");
				}
			}
		}
	}
}