package nautilus.game.arcade.game.games.minestrike;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.minestrike.data.Bullet;

public class CustomGunDamageEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
	
	private Bullet _bullet;
	private final boolean _headshot;
	private CustomDamageEvent _damageEvent;
	private GunModule _game;

	public CustomGunDamageEvent(Bullet _bullet, Player _target, boolean _headshot, CustomDamageEvent _damageEvent, GunModule game)
	{
		super(_target);
		this._bullet = _bullet;
		this._headshot = _headshot;
		this._damageEvent = _damageEvent;
		this._game = game;
	}
	
	public Bullet getBullet()
	{
		return _bullet;
	}
	
	public boolean isHeadshot()
	{
		return _headshot;
	}
	
	public CustomDamageEvent getDamageEvent()
	{
		return _damageEvent;
	}
	
	public GunModule getGame()
	{
		return _game;
	}
	
}