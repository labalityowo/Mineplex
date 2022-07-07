package mineplex.gemhunters.playerstatus;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.spawn.event.PlayerTeleportIntoMapEvent;

@ReflectivelyCreateMiniPlugin
public class PlayerStatusModule extends MiniClientPlugin<PlayerStatus>
{

	private static final long COMBAT_TIME = TimeUnit.SECONDS.toMillis(30);

	private final PlayerStatus _default;

	public PlayerStatusModule()
	{
		super("Player Status");

		_default = new PlayerStatus(PlayerStatusType.DANGER);
	}

	@Override
	protected PlayerStatus addPlayer(UUID uuid)
	{
		return _default;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			PlayerStatus status = Get(player);

			if (status.isDone())
			{
				Set(player, _default);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void entityDamage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled() || !(event.getEntity() instanceof Player))
		{
			return;
		}

		if (event.getDamager() instanceof Player)
		{
			setStatus((Player) event.getDamager(), PlayerStatusType.COMBAT, COMBAT_TIME);
		}
		
		setStatus((Player) event.getEntity(), PlayerStatusType.COMBAT, COMBAT_TIME);
	}
	
	@EventHandler
	public void teleportMap(PlayerTeleportIntoMapEvent event)
	{
		setStatus(event.getPlayer(), PlayerStatusType.DANGER);
	}

	public void setStatus(Player player, PlayerStatusType statusType)
	{
		setStatus(player, statusType, false);
	}

	public void setStatus(Player player, PlayerStatusType statusType, long length)
	{
		setStatus(player, statusType, length, false);
	}

	public void setStatus(Player player, PlayerStatusType statusType, boolean force)
	{
		setStatus(player, statusType, -1, force);
	}

	public void setStatus(Player player, PlayerStatusType statusType, long length, boolean force)
	{
		PlayerStatus current = Get(player);
		//Bukkit.broadcastMessage("Setting " + player.getName() + " -> " + statusType.getName() + " -> " + length);

		if (!force && current.getStatusType().hasPriority(statusType))
		{
			return;
		}

		Set(player, new PlayerStatus(statusType, length));
	}

}
