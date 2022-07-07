package mineplex.game.clans.clans.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.game.clans.clans.data.PlayerClan;

public class ClansPlayerDeathEvent extends Event
{
	private PlayerDeathEvent _event;
	private PlayerClan _player;
	private PlayerClan _killer;

	public ClansPlayerDeathEvent(PlayerDeathEvent event, PlayerClan player, PlayerClan killer)
	{
		_event = event;
		_player = player;
		_killer = killer;
	}

	public PlayerDeathEvent getEvent()
	{
		return _event;
	}

	public PlayerClan getPlayer()
	{
		return _player;
	}

	public PlayerClan getKiller()
	{
		return _killer;
	}

	public void setPlayer(PlayerClan player)
	{
		_player = player;
	}

	public void setKiller(PlayerClan killer)
	{
		_killer = killer;
	}

	// Bukkit event stuff
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }
}
