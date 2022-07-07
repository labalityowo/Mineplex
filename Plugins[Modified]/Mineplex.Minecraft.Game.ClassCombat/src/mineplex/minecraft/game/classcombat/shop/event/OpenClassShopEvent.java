package mineplex.minecraft.game.classcombat.shop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class OpenClassShopEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }

	public OpenClassShopEvent(Player who)
	{
		super(who);
	}

}
