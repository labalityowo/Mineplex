package nautilus.game.arcade.game.games.typewars;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.typewars.TypeWars.KillType;

public class StaffKillMonitorManager implements Listener
{
	public enum Perm implements Permission
	{
		MONITOR_KILLS_COMMAND,
	}

	private TypeWars _host;
	private String _command = "/words";
	
	private NautHashMap<Player, Player> _monitoring = new NautHashMap<Player, Player>();
	
	public StaffKillMonitorManager(TypeWars host)
	{
		_host = host;
		
		host.Manager.registerEvents(this);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.TRAINEE.setPermission(Perm.MONITOR_KILLS_COMMAND, true, true);
	}
	
	public TypeWars getHost()
	{
		return _host;
	}
	
	@EventHandler
	public void onEnd(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End)
		{
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler
	public void onKill(MinionKillEvent event)
	{
		if (event.getType() == KillType.SPELL)
			return;
		
		for (Player staff : _monitoring.keySet())
		{
			if (_monitoring.get(staff) == event.getPlayer())
			{
				UtilPlayer.message(staff, F.elem(C.cRedB + ">>") + F.name(event.getPlayer().getName()) + " killed " + F.elem(C.cGold + event.getMinion().getName()) + ".");
			}
		}
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event)
	{
		if (!_host.IsLive())
			return;
		
		if (!_host.Manager.GetClients().Get(event.getPlayer()).hasPermission(Perm.MONITOR_KILLS_COMMAND))
			return;
		
		event.setCancelled(true);
		
		String[] message = event.getMessage().toLowerCase().split(" ");
		if (message.length == 0 || !message[0].equalsIgnoreCase(_command))
			return;
		
		if (message.length == 1)
		{
			if (_monitoring.containsKey(event.getPlayer()))
			{
				UtilPlayer.message(event.getPlayer(), F.main("TypeWars", "You are no longer monitoring " + F.name(_monitoring.remove(event.getPlayer()).getName()) + "."));
			}
			else
			{
				UtilPlayer.message(event.getPlayer(), F.main("TypeWars", "Please enter a player to monitor!"));
			}
			return;
		}
		
		String playerName = message[1];
		
		Player player = UtilPlayer.searchOnline(event.getPlayer(), playerName, true);
		if (player == null)
			return;
		
		_monitoring.put(event.getPlayer(), player);
		UtilPlayer.message(event.getPlayer(), F.main("TypeWars", "You are now monitoring " + F.name(player.getName()) + ". Type " + F.elem(_command) + " to stop."));
	}
}