package nautilus.game.arcade.game.games.uhc.components;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.uhc.UHC;

public class UHCFreezer implements Listener
{

	private UHC _host;
	
	private Set<UUID> _frozenPlayers = new HashSet<>();
	
	public UHCFreezer(UHC host)
	{
		_host =  host;
		
		host.Manager.registerEvents(this);
	}
	
	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}
				
		UtilServer.Unregister(this);
	}
	
	public void freeze(Player player)
	{
		_frozenPlayers.add(player.getUniqueId());
	}
	
	public void unfreeze(Player player)
	{
		_frozenPlayers.remove(player.getUniqueId());
	}
	
	public void unfreeze()
	{
		_frozenPlayers.clear();
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (!_host.IsLive() || !_frozenPlayers.contains(event.getPlayer().getUniqueId()))
		{
			return;
		}
				
		Location from = event.getFrom();
		Location to = event.getTo();
		
		if (from.getX() == to.getX() && from.getZ() == to.getZ())
		{
			return;
		}
		
		event.setTo(from);
	}
	
}
