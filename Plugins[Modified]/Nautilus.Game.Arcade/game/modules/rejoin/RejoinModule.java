package nautilus.game.arcade.game.modules.rejoin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilServer;
import mineplex.core.game.rejoin.GameRejoinManager;
import mineplex.core.portal.events.ServerTransferEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.kit.Kit;

public class RejoinModule extends Module
{

	private final GameRejoinManager _manager;
	private final Map<UUID, PlayerGameInfo> _rejoinData;
	private final Set<Player> _disallow;

	private boolean _saveInventory;

	public RejoinModule(ArcadeManager manager)
	{
		_manager = new GameRejoinManager(manager);
		_rejoinData = new HashMap<>();
		_disallow = new HashSet<>();
	}

	@Override
	protected void setup()
	{
		getGame().QuitOut = false;
	}

	@Override
	public void cleanup()
	{
		_rejoinData.clear();
		_disallow.clear();
		UtilServer.Unregister(_manager);
	}

	public RejoinModule setSaveInventory(boolean saveInventory)
	{
		_saveInventory = saveInventory;
		return this;
	}

	public void disableRejoining()
	{
		getGame().QuitOut = true;
		_rejoinData.clear();
		_manager.searchToRejoin();
	}

	@EventHandler
	public void playerKick(PlayerKickEvent event)
	{
		if (!isEnabled())
		{
			return;
		}

		_disallow.add(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTransfer(ServerTransferEvent event)
	{
		if (!isEnabled())
		{
			return;
		}

		_disallow.add(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerQuit(PlayerQuitEvent event)
	{
		if (!isEnabled())
		{
			return;
		}

		Player player = event.getPlayer();

		if (_disallow.remove(player) || player.isDead() || !player.getWorld().equals(getGame().WorldData.World))
		{
			return;
		}

		GameTeam team = getGame().GetTeam(player);

		if (team == null || !team.IsAlive(player))
		{
			return;
		}

		PlayerGameInfo data;

		if (_saveInventory)
		{
			data = new PlayerGameInfo(player.getHealth(), player.getInventory().getContents(), player.getInventory().getArmorContents(), getGame().GetKit(player), team);
		}
		else
		{
			data = new PlayerGameInfo(player.getHealth(), getGame().GetKit(player), team);
		}

		_rejoinData.put(player.getUniqueId(), data);

		team.RemovePlayer(player);

		// For MPS, allow them to rejoin but don't bother sending them the message
		if (!getGame().getArcadeManager().GetGameHostManager().isPrivateServer())
		{
			_manager.saveRejoinData(player, getGame().GetType().getGameId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerQuitMonitor(PlayerQuitEvent event)
	{
		_disallow.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerLogin(PlayerLoginEvent event)
	{
		if (!isEnabled() || event.getResult() != Result.ALLOWED)
		{
			return;
		}

		Player player = event.getPlayer();
		PlayerGameInfo info = _rejoinData.get(player.getUniqueId());

		if (info == null || getGame().getArcadeManager().isVanished(player))
		{
			return;
		}

		PlayerRejoinGameEvent rejoinEvent = new PlayerRejoinGameEvent(player, info);
		UtilServer.CallEvent(rejoinEvent);

		if (rejoinEvent.isCancelled())
		{
			info._cancelled = true;
			return;
		}

		if (info.getTeam() != null)
		{
			info.getTeam().AddPlayer(player, true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(PlayerJoinEvent event)
	{
		if (!isEnabled())
		{
			return;
		}

		Player player = event.getPlayer();
		PlayerGameInfo info = _rejoinData.remove(player.getUniqueId());

		if (info == null || info.isCancelled() || getGame().getArcadeManager().isVanished(player))
		{
			return;
		}

		if (info.getKit() != null)
		{
			getGame().SetKit(player, info.getKit(), false);
			getGame().ValidateKit(player, info.getTeam());
		}

		player.setHealth(Math.min(info.getHealth(), player.getHealth()));

		if (_saveInventory)
		{
			player.getInventory().setArmorContents(info.getInventoryArmour());
			player.getInventory().setContents(info.getInventoryContents());
		}
	}

	private boolean isEnabled()
	{
		return getGame().InProgress() && !getGame().QuitOut;
	}

	public class PlayerGameInfo
	{
		private final double _health;
		private final ItemStack[] _inventoryContents, _inventoryArmour;
		private final Kit _kit;
		private GameTeam _team;
		private boolean _cancelled;

		PlayerGameInfo(double health, Kit kit, GameTeam team)
		{
			this(health, null, null, kit, team);
		}

		PlayerGameInfo(double health, ItemStack[] inventoryContents, ItemStack[] inventoryArmour, Kit kit, GameTeam team)
		{
			_health = health;
			_inventoryContents = inventoryContents;
			_inventoryArmour = inventoryArmour;
			_kit = kit;
			_team = team;
		}

		public double getHealth()
		{
			return _health;
		}

		public ItemStack[] getInventoryContents()
		{
			return _inventoryContents;
		}

		public ItemStack[] getInventoryArmour()
		{
			return _inventoryArmour;
		}

		public Kit getKit()
		{
			return _kit;
		}

		public void setTeam(GameTeam team)
		{
			_team = team;
		}

		public GameTeam getTeam()
		{
			return _team;
		}

		public boolean isCancelled()
		{
			return _cancelled;
		}
	}
}
