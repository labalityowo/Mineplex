package mineplex.game.clans.clans.event;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanInfo;

public class PlayerUnClaimTerritoryEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _unClaimer;
	private Chunk _unClaimedChunk;
	private ClanInfo _clan;
	
	private boolean _cancelled;
	
	public PlayerUnClaimTerritoryEvent(Player unClaimer, Chunk unClaimedChunk, ClanInfo clan)
	{
		_unClaimer = unClaimer;
		_unClaimedChunk = unClaimedChunk;
		_clan = clan;
	}
	
	public Player getUnClaimer()
	{
		return _unClaimer;
	}
	
	public Chunk getUnClaimedChunk()
	{
		return _unClaimedChunk;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	public ClanInfo getClan()
	{
		return _clan;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
}