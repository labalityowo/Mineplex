package nautilus.game.arcade.game.games.moba.kit.hp;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class MobaHPRegenEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList _handlers = new HandlerList();

	private final Player _source;
	private final double _initialHealth;
	private double _health;
	private boolean _natural;
	private boolean _cancel;

	public MobaHPRegenEvent(Player who, Player source, double health, boolean natural)
	{
		super(who);

		_source = source;
		_initialHealth = health;
		_health = health;
		_natural = natural;
	}

	public Player getSource()
	{
		return _source;
	}

	public void setHealth(double health)
	{
		_health = health;
	}

	public void increaseHealth(double factor)
	{
		_health = _initialHealth + (_initialHealth * factor);
	}

	public double getHealth()
	{
		return _health;
	}

	public void setNatural(boolean natural)
	{
		_natural = natural;
	}

	public boolean isNatural()
	{
		return _natural;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancel;
	}

	@Override
	public void setCancelled(boolean b)
	{
		_cancel = b;
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
