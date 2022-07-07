package nautilus.game.arcade.game.games.moba.kit;

import mineplex.core.common.entity.ClientArmorStand;
import nautilus.game.arcade.game.games.moba.MobaRole;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RoleSelectEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList _handlers = new HandlerList();

	private final ClientArmorStand _stand;
	private final MobaRole _role;

	private boolean _cancel;

	public RoleSelectEvent(Player who, ClientArmorStand stand, MobaRole role)
	{
		super(who);

		_stand = stand;
		_role = role;
	}

	public ClientArmorStand getStand()
	{
		return _stand;
	}

	public MobaRole getRole()
	{
		return _role;
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
