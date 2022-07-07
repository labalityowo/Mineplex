package nautilus.game.arcade.game.games.moba.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class AmmoGiveEvent extends PlayerEvent
{

	private static final HandlerList _handlers = new HandlerList();

	private int _ammoToGive;
	private int _maxAmmo;

	public AmmoGiveEvent(Player who, int ammoToGive, int maxAmmo)
	{
		super(who);

		_ammoToGive = ammoToGive;
		_maxAmmo = maxAmmo;
	}

	public void setAmmoToGive(int ammo)
	{
		_ammoToGive = ammo;
	}

	public int getAmmoToGive()
	{
		return _ammoToGive;
	}

	public void setMaxAmmo(int ammo)
	{
		_maxAmmo = ammo;
	}

	public int getMaxAmmo()
	{
		return _maxAmmo;
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
