package mineplex.core.gadget.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.core.gadget.types.Gadget;

public class PlayerUseCoalEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();

	private final CoalReward _prize;
	private final int _cost;

	public PlayerUseCoalEvent(Player player, CoalReward reward, int cost)
	{
		super(player);
		this._prize = reward;
		this._cost = cost;
	}

	public CoalReward getPrize()
	{
		return this._prize;
	}

	public int getCost()
	{
		return this._cost;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public enum CoalReward
	{
		HAT,
		PET,
		PARTICLE
	}
}
