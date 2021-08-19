package mineplex.core.teleport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.ClientUnloadEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.jsonchat.ChildJsonMessage;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.teleport.command.LocateCommand;
import mineplex.core.teleport.command.ModLocateCommand;
import mineplex.core.teleport.command.TeleportCommand;
import mineplex.core.teleport.command.TraineeLocateCommand;
import mineplex.core.teleport.event.MineplexTeleportEvent;
import mineplex.core.teleport.redis.RankLocate;
import mineplex.core.teleport.redis.RankLocateCallback;
import mineplex.core.teleport.redis.RedisLocate;
import mineplex.core.teleport.redis.RedisLocateCallback;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.commands.ServerCommandManager;

public class Teleport extends MiniPlugin
{
	public enum Perm implements Permission
	{
		FIND_COMMAND,
		FIND_MOD_COMMAND,
		FIND_TRAINEE_COMMAND,
		TELEPORT_COMMAND,
		TELEPORT_LOCATION_COMMAND,
		TELEPORT_OTHER_COMMAND,
		TELEPORT_ALL_COMMAND,
	}

	private Map<UUID, Integer> _failedRedisLocates = new HashMap<>();
	private Map<UUID, Integer> _failedRankLocates = new HashMap<>();

	private LinkedList<Teleporter> teleportList = new LinkedList<>();
	private Map<String, LinkedList<Location>> _tpHistory = new HashMap<>();
	private String _serverName;
	private CoreClientManager _clientManager;

	public Teleport(JavaPlugin plugin, CoreClientManager clientManager)
	{
		super("Teleport", plugin);

		_serverName = getPlugin().getConfig().getString("serverstatus.name");
		_clientManager = clientManager;

		ServerCommandManager.getInstance().registerCommandType(RankLocate.class, command ->
		{
			runSync(() ->
			{
				PermissionGroup group = PermissionGroup.valueOf(command.getRankIdentifier());
				Set<String> on = new HashSet<>();
				for (Player online : Bukkit.getOnlinePlayers())
				{
					if (_clientManager.Get(online).getPrimaryGroup().equals(group))
					{
						on.add(online.getName());
					}
				}
				if (!on.isEmpty())
				{
					new RankLocateCallback(command, _serverName, on).publish();
				}
			});
		});
		ServerCommandManager.getInstance().registerCommandType(RankLocateCallback.class, command ->
		{
			runSync(() ->
			{
				Player p = Bukkit.getPlayer(command.getReceivingPlayerUUID());

				Integer taskId = _failedRankLocates.remove(command.getUUID());
				if (taskId != null)
				{
					getScheduler().cancelTask(taskId);
					UtilPlayer.message(p, F.main("Locate", "All Online:"));
				}

				if (p == null)
				{
					return;
				}

				UtilPlayer.message(p, C.cBlue + "- " + C.cGray + command.getServerName());
				for (String on : command.getOnline())
				{
					ChildJsonMessage message = new JsonMessage("").extra(C.cGold + " - " + C.cYellow + on);
					message.click(ClickEvent.RUN_COMMAND, "/server " + command.getServerName());
					message.hover(HoverEvent.SHOW_TEXT, "Teleport to " + command.getServerName());

					message.sendToPlayer(p);
				}
			});
		});
		ServerCommandManager.getInstance().registerCommandType("RedisLocate", RedisLocate.class, command ->
		{
			runSync(() ->
			{
				Player target = Bukkit.getPlayerExact(command.getTarget());
				if (target != null)
				{
					RedisLocateCallback callback = new RedisLocateCallback(command, _serverName, target.getName());
					callback.publish();
				}
			});
		});
		ServerCommandManager.getInstance().registerCommandType("RedisLocateCallback", RedisLocateCallback.class, callback ->
		{
			runSync(() ->
			{
				Integer taskId = _failedRedisLocates.remove(callback.getUUID());
				if (taskId != null)
				{
					getScheduler().cancelTask(taskId);
				}

				Player player = Bukkit.getPlayer(callback.getReceivingPlayerId());

				if (player != null)
				{
					ChildJsonMessage message = new JsonMessage("").extra(C.mHead + "Locate" + "> " + C.mBody + "Located [" + C.mElem
							+ callback.getLocatedPlayer() + C.mBody + "] at ");

					message.add(C.cBlue + callback.getServer()).click(ClickEvent.RUN_COMMAND,
							"/server " + callback.getServer());

					message.hover(HoverEvent.SHOW_TEXT, "Teleport to " + callback.getServer());

					message.sendToPlayer(player);
				}
			});
		});

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.TRAINEE.setPermission(Perm.FIND_COMMAND, true, true);
		PermissionGroup.MA.setPermission(Perm.FIND_MOD_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.FIND_MOD_COMMAND, true, true);
		PermissionGroup.MA.setPermission(Perm.FIND_TRAINEE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.FIND_TRAINEE_COMMAND, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.TELEPORT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TELEPORT_LOCATION_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TELEPORT_OTHER_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TELEPORT_ALL_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new TeleportCommand(this));
		addCommand(new LocateCommand(this));
		addCommand(new ModLocateCommand(this));
		addCommand(new TraineeLocateCommand(this));
	}

	public void locatePlayer(final Player player, final String target)
	{
		Player targetPlayer = Bukkit.getPlayerExact(target);

		if (targetPlayer != null)
		{
			UtilPlayer.message(player, F.main("Locate", C.mBody + " [" + C.mElem + target + C.mBody + "] is in the same server!"));
			return;
		}

		RedisLocate locate = new RedisLocate(_serverName, player.getName(), player.getUniqueId(), target);
		locate.publish();

		int id = getScheduler().runTaskLater(_plugin, () ->
		{
			_failedRedisLocates.remove(locate.getUUID());
			UtilPlayer.message(player, F.main("Locate", C.mBody + "Failed to locate [" + C.mElem + target + C.mBody + "]."));
		}, 40L).getTaskId();

		_failedRedisLocates.put(locate.getUUID(), id);
	}

	public void locateRank(final Player sender, final PermissionGroup group)
	{
		if (group == null)
		{
			UtilPlayer.message(sender, F.main("Locate", "That group does not exist!"));
			return;
		}

		RankLocate locate = new RankLocate(_serverName, sender.getName(), sender.getUniqueId(), group.name());
		locate.publish();
		int id = getScheduler().runTaskLater(_plugin, () ->
		{
			_failedRankLocates.remove(locate.getUUID());
			UtilPlayer.message(sender, F.main("Locate", "There are no members of that group online!"));
		}, 60L).getTaskId();

		_failedRankLocates.put(locate.getUUID(), id);
	}

	@EventHandler
	public void unloadHistory(ClientUnloadEvent event)
	{
		_tpHistory.remove(event.GetName());
	}

	@EventHandler
	public void on(WorldUnloadEvent event)
	{
		_tpHistory.values().forEach(list -> list.removeIf(location -> location.getWorld() == event.getWorld()));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (teleportList.isEmpty())
			return;

		teleportList.removeFirst().doTeleport();
	}

	public void playerToPlayer(Player caller, String target, String to)
	{
		LinkedList<Player> listA = new LinkedList<Player>();

		//ALL
		if (target.equals("%ALL%"))
		{
			for (Player cur : UtilServer.getPlayers())
			{
				listA.add(cur);
			}
		}
		//Normal
		else
		{
			listA = UtilPlayer.matchOnline(caller, target, true);
		}

		//To
		Player pB = UtilPlayer.searchOnline(caller, to, true);

		if (listA.isEmpty() || pB == null)
		{
			return;
		}

		if (listA.size() == 1)
		{
			Player pA = listA.getFirst();

			String mA = null;
			String mB = null;

			//Inform
			if (pA.equals(caller))
			{
				mA = F.main("Teleport", "You teleported to " + F.elem(pB.getName()) + ".");
			}
			else if (pB.equals(caller))
			{
				mA = F.main("Teleport", F.elem(caller.getName()) + " teleported you to themself.");
				mB = F.main("Teleport", "You teleported " + F.elem(pA.getName()) + " to yourself.");
			}
			else
			{
				mA = F.main("Teleport", F.elem(caller.getName()) + " teleported you to " + F.elem(pB.getName()) + ".");
				mB = F.main("Teleport", "You teleported " + F.elem(pA.getName()) + " to " + F.elem(pB.getName()) + ".");
			}

			//Register
			Add(pA, pB.getLocation(), mA, true, caller, mB,
					pA.getName() + " teleported to " + pB.getName() + " via " + caller.getName());
			return;
		}

		boolean first = true;
		for (Player pA : listA)
		{
			String mA = null;
			String mB = null;

			//Inform
			if (pA.equals(caller))
			{
				mA = F.main("Teleport", "You teleported to " + F.elem(pB.getName()) + ".");
			}
			else if (pB.equals(caller))
			{
				mA = F.main("Teleport", F.elem(caller.getName()) + " teleported you to themself.");
				mB = F.main("Teleport", "You teleported " + F.elem(listA.size() + " Players") + " to yourself.");
			}
			else
			{
				mA = F.main("Teleport", F.elem(caller.getName()) + " teleported you to " + F.elem(pB.getName()) + ".");
				mB = F.main("Teleport", "You teleported " + F.elem(listA.size() + " Players") + " to " + F.elem(pB.getName()) + ".");
			}

			//Register
			if (first)
			{
				Add(pA, pB.getLocation(), mA, true, caller, mB, pA.getName() + " teleported to " + pB.getName() + " via " + caller.getName());
			}
			else
			{
				Add(pA, pB.getLocation(), mA, true, caller, null, pA.getName() + " teleported to " + pB.getName() + " via " + caller.getName());
			}

			first = false;
		}
	}

	public void playerToLoc(Player caller, String target, String sX, String sY, String sZ)
	{
		playerToLoc(caller, target, caller.getWorld().getName(), sX, sY, sZ);
	}

	public void playerToLoc(Player caller, String target, String world, String sX, String sY, String sZ)
	{
		Player player = UtilPlayer.searchOnline(caller, target, true);

		if (player == null)
		{
			return;
		}

		try
		{
			int x = sX.matches(".*[0-9]") ? Integer.parseInt(sX.replace("~", "")) : 0;
			int y = sY.matches(".*[0-9]") ? Integer.parseInt(sY.replace("~", "")) : 0;
			int z = sZ.matches(".*[0-9]") ? Integer.parseInt(sZ.replace("~", "")) : 0;

			Location pLoc = player.getLocation();

			if (sX.startsWith("~"))
			{
				x += pLoc.getBlockX();
			}

			if (sY.startsWith("~"))
			{
				y += pLoc.getBlockY();
			}

			if (sZ.startsWith("~"))
			{
				z += pLoc.getBlockZ();
			}

			Location loc = new Location(Bukkit.getWorld(world), x, y, z);

			//Inform
			String mA = null;
			if (caller == player)
			{
				mA = F.main("Teleport", "You teleported to " + UtilWorld.locToStrClean(loc) + ".");
			}
			else
			{
				mA = F.main("Teleport", F.elem(caller.getName()) + " teleported you to " + UtilWorld.locToStrClean(loc) + ".");
			}

			//Register
			Add(player, loc, mA, true, caller, null, player.getName() + " teleported to " + UtilWorld.locToStrClean(loc) + " via " + caller.getName());
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main("Teleport", "Invalid Location [" + sX + "," + sY + "," + sZ + "]."));
			return;
		}
	}

	public void Add(Player pA, Location loc, String mA, boolean record, Player pB, String mB, String log)
	{
		teleportList.addLast(new Teleporter(this, pA, pB, mA, mB, loc, record, log));
	}

	public void TP(Player player, Location getLocation)
	{
		TP(player, getLocation, true);
	}

	public void TP(Player player, Location loc, boolean dettach)
	{
		//Event
		MineplexTeleportEvent event = new MineplexTeleportEvent(player, loc);
		UtilServer.getServer().getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		_tpHistory.computeIfAbsent(player.getName(), key -> new LinkedList<>()).addFirst(player.getLocation());

		if (dettach)
		{
			player.eject();
			player.leaveVehicle();
		}

		player.setFallDistance(0);
		UtilAction.zeroVelocity(player);

		player.teleport(loc);
	}

	public LinkedList<Location> GetTPHistory(Player player)
	{
		return _tpHistory.get(player.getName());
	}

	public String getServerName()
	{
		return _serverName;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}
}