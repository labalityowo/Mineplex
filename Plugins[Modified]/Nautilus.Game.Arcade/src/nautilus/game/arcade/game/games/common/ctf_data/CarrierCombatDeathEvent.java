package nautilus.game.arcade.game.games.common.ctf_data;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CarrierCombatDeathEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Player _killed;
	private Player _killer;
	
	public CarrierCombatDeathEvent(Player killed, Player killer)
	{
		_killed = killed;
		_killer = killer;
	}
	
    public HandlerList getHandlers()
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    public Player GetPlayer(boolean killer)
    {
    	if (killer)
    		return _killer;
    	else
    		return _killed;
    }
}