package mineplex.game.clans.clans.worldevent.raid;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;

public abstract class RaidChallenge<T extends RaidWorldEvent> implements Listener
{
	private T _raid;
	private String _name;
	private boolean _completed;
	
	public RaidChallenge(T raid, String name)
	{
		_raid = raid;
		_name = name;
		_completed = false;
	}
	
	public abstract void customStart();
	public abstract void customComplete();
	
	public T getRaid()
	{
		return _raid;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isComplete()
	{
		return _completed;
	}
	
	public void start()
	{
		customStart();
		UtilServer.RegisterEvents(this);
		UtilTextMiddle.display("", getName(), getRaid().getPlayers().toArray(new Player[getRaid().getPlayers().size()]));
	}
	
	public void complete()
	{
		complete(true);
	}
	
	public void complete(boolean allowCustom)
	{
		if (allowCustom)
		{
			customComplete();
		}
		HandlerList.unregisterAll(this);
		_completed = true;
	}
}