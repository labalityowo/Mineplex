package nautilus.game.arcade.game.modules.compass.menu.button;

import java.lang.ref.WeakReference;

import mineplex.core.common.util.F;
import mineplex.core.shop.item.IButton;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.modules.compass.CompassEntry;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * Created by shaun on 14-09-26.
 */
public class CompassButton implements IButton
{
	private ArcadeManager _arcadeManager;
	private Player _player;
	private WeakReference<Entity> _target;

	public CompassButton(ArcadeManager arcadeManager, Player player, CompassEntry target)
	{
		_arcadeManager = arcadeManager;
		_player = player;
		_target = new WeakReference<>(target.getEntity());
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		// Make sure this player is still a spectator
		if (!((CraftPlayer) player).getHandle().spectating)
			return;

		if (_target.get() == null)
		{
			_player.sendMessage(F.main("Spectate", "That target does not exist anymore"));
			return;
		}

		Entity entity = _target.get();
		if (entity instanceof Player)
		{
			if (_arcadeManager.IsAlive((Player) entity))
			{
				if (clickType == ClickType.RIGHT)
				{
					_player.closeInventory();
					_arcadeManager.getGameSpectatorManager().setSpectating(_player, entity);
				}
				else
				{
					_player.teleport(entity.getLocation().add(0, 1, 0));
				}
			}
			else
			{
				_player.sendMessage(F.main("Spectate", F.name(entity.getName()) + " is no longer alive."));
			}
		}
		else
		{
			if (clickType == ClickType.RIGHT)
			{
				_player.closeInventory();
				_arcadeManager.getGameSpectatorManager().setSpectating(_player, entity);
			}
			else
			{
				_player.teleport(entity.getLocation().add(0, 1, 0));
			}
		}
	}
}