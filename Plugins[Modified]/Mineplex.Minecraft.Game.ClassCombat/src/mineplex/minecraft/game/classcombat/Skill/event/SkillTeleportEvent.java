package mineplex.minecraft.game.classcombat.Skill.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkillTeleportEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }
	
	private boolean _cancelled;
	public boolean isCancelled() { return _cancelled; }
	public void setCancelled(boolean cancelled) { _cancelled = cancelled; }
	
	private Player _player;
	public Player getPlayer() { return _player; }
	
	private Location _destination;
	public Location getDestination() { return _destination; }

    public SkillTeleportEvent(Player player, Location destination)
    {
    	_player = player;
    	_destination = destination;
    }
}
