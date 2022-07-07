package mineplex.gemhunters.death.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.gemhunters.death.quitnpc.QuitNPC;

public class QuitNPCDespawnEvent extends Event implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();

	private final QuitNPC _npc;
	private final boolean _pluginRemove;
	private boolean _cancel;

	public QuitNPCDespawnEvent(QuitNPC npc, boolean pluginRemove)
	{
		_npc = npc;
		_pluginRemove = pluginRemove;
	}

	public QuitNPC getNpc()
	{
		return _npc;
	}

	public boolean isPluginRemove()
	{
		return _pluginRemove;
	}

	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancel;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancel = cancel;
	}

}
