package mineplex.game.clans.clans.event;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.clans.ClanInfo;

public class PlayerPreClaimTerritoryEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _claimer;
	private Chunk _claimedChunk;
	
	private boolean _cancelled;

	private ClanInfo _clan;
	
	public PlayerPreClaimTerritoryEvent(Player claimer, Chunk claimedChunk, ClanInfo clan)
	{
		_claimer = claimer;
		_claimedChunk = claimedChunk;
		_clan = clan;
	}
	
	public Player getClaimer()
	{
		return _claimer;
	}
	
	public ClanInfo getClan()
	{
		return _clan;
	}
	
	public Chunk getClaimedChunk()
	{
		return _claimedChunk;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
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