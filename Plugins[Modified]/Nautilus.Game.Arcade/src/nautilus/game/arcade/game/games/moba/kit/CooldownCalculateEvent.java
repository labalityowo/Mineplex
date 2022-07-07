package nautilus.game.arcade.game.games.moba.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class CooldownCalculateEvent extends PlayerEvent
{

	private static final HandlerList _handlers = new HandlerList();

	private final long _initialCooldown;
	private final String _ability;
	private long _cooldown;

	public CooldownCalculateEvent(Player who, String ability, long cooldown)
	{
		super(who);

		_initialCooldown = cooldown;
		_ability = ability;
		_cooldown = cooldown;
	}

	public String getAbility()
	{
		return _ability;
	}

	public void setCooldown(long cooldown)
	{
		_cooldown = cooldown;
	}

	public void decreaseCooldown(double factor)
	{
		_cooldown -= (long) ((double) _initialCooldown * factor);
	}

	public long getCooldown()
	{
		return _cooldown;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
