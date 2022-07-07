package mineplex.clanshub.queue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mineplex.clansqueue.common.ClansQueueMessenger;
import com.mineplex.clansqueue.common.QueueConstant;
import com.mineplex.clansqueue.common.messages.PlayerJoinQueueCallbackMessage;
import com.mineplex.clansqueue.common.messages.PlayerJoinQueueMessage;
import com.mineplex.clansqueue.common.messages.PlayerLeaveQueueMessage;
import com.mineplex.clansqueue.common.messages.PlayerSendToServerMessage;
import com.mineplex.clansqueue.common.messages.QueueDeleteMessage;
import com.mineplex.clansqueue.common.messages.QueuePauseBroadcastMessage;
import com.mineplex.clansqueue.common.messages.QueuePauseUpdateMessage;
import com.mineplex.clansqueue.common.messages.QueueStatusMessage;

import mineplex.clanshub.ClansTransferManager;
import mineplex.clanshub.ServerInfo;
import mineplex.clanshub.queue.data.ClansQueueData;
import mineplex.clanshub.queue.data.QueuePlayerData;
import mineplex.core.Managers;
import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.portal.events.ServerTransferEvent;
import mineplex.core.punish.clans.ClansBanManager;
import mineplex.core.recharge.Recharge;

@ReflectivelyCreateMiniPlugin
public class HubQueueManager extends MiniClientPlugin<QueuePlayerData>
{
	public enum Perm implements Permission
	{
		JOIN_PAUSED_QUEUE,
		TOGGLE_QUEUE_PAUSE,
		LIST_QUEUES,
	}
	
	public enum QueuePriority implements Permission
	{
		BYPASS(QueueConstant.BYPASS_QUEUE_WEIGHT, PermissionGroup.CONTENT, PermissionGroup.TRAINEE),
		PRIORITY(7, PermissionGroup.BUILDER),
		ETERNAL(6, PermissionGroup.ETERNAL),
		TITAN(5, PermissionGroup.TITAN),
		LEGEND(4, PermissionGroup.LEGEND),
		HERO(3, PermissionGroup.HERO),
		ULTRA(2, PermissionGroup.ULTRA),
		DEFAULT(1, PermissionGroup.PLAYER)
		;
		
		private final int _weight;
		private final List<PermissionGroup> _granted;
		
		private QueuePriority(int weight, PermissionGroup... granted)
		{
			_weight = weight;
			_granted = Collections.unmodifiableList(Arrays.asList(granted));
		}
		
		public int getWeight()
		{
			return _weight;
		}
		
		public List<PermissionGroup> getGranted()
		{
			return _granted;
		}
	}
	
	private final CoreClientManager _clientManager = require(CoreClientManager.class);
	private final ClansBanManager _punish = require(ClansBanManager.class);
	private final Portal _portal = require(Portal.class);
	private final Comparator<QueuePriority> _prioritySorter = (q1, q2) ->
	{
		if (q1.getWeight() == -1 && q2.getWeight() != -1)
		{
			return -1;
		}
		if (q2.getWeight() == -1 && q1.getWeight() != -1)
		{
			return 1;
		}
		
		return Integer.compare(q2.getWeight(), q1.getWeight());
	};
	private final Map<String, ClansQueueData> _queueData = new HashMap<>();
	private final ClansQueueMessenger _messenger;
	
	private HubQueueManager()
	{
		super("Queue Manager");
		
		generatePermissions();
		_messenger = ClansQueueMessenger.getMessenger(UtilServer.getServerName());
		
		_messenger.registerListener(PlayerJoinQueueCallbackMessage.class, (callback, origin) ->
		{
			runSync(() ->
			{
				Player player = Bukkit.getPlayer(callback.PlayerUUID);
				if (player != null)
				{
					QueuePlayerData data = Get(player);
					data.Queued = true;
					data.QueuePosition = callback.Position;
					UtilPlayer.message(player, F.main(getName(), "You have joined the queue for server " + F.elem(data.TargetServer) + "! Your position: " + F.greenElem("#" + data.QueuePosition)));
				}
			});
		});
		_messenger.registerListener(PlayerSendToServerMessage.class, (callback, origin) ->
		{
			runSync(() ->
			{
				Player player = Bukkit.getPlayer(callback.PlayerUUID);
				if (player != null)
				{
					Get(player).Queued = false;
					player.leaveVehicle();
					player.eject();
					_portal.sendPlayerToServer(player, callback.TargetServer, Intent.FORCE_TRANSFER);
				}
			});
		});
		_messenger.registerListener(QueueStatusMessage.class, (status, origin) ->
		{
			runSync(() ->
			{
				status.Snapshots.forEach(snapshot ->
				{
					ClansQueueData data = _queueData.computeIfAbsent(snapshot.ServerName, (name) -> new ClansQueueData(name));
					
					data.QueueMembers = snapshot.Queue.size();
					data.QueuePaused = snapshot.Paused;
					snapshot.Queue.entrySet().forEach(entry ->
					{
						Player player = Bukkit.getPlayer(entry.getKey());
						if (player != null)
						{
							Get(player).QueuePosition = entry.getValue();
							if (Recharge.Instance.use(player, "Queue Status Update", 7000, false, false))
							{
								UtilPlayer.message(player, F.main(getName(), "Your position: " + F.greenElem("#" + entry.getValue())));
							}
						}
					});
				});
			});
		});
		_messenger.registerListener(QueuePauseBroadcastMessage.class, (broadcast, origin) ->
		{
			runSync(() ->
			{
				ClansQueueData data = _queueData.computeIfAbsent(broadcast.ServerName, (name) -> new ClansQueueData(name));
				data.QueuePaused = broadcast.Paused;
				GetValues().forEach(qp ->
				{
					if (qp.TargetServer != null && qp.TargetServer.equals(broadcast.ServerName))
					{
						UtilPlayer.message(Bukkit.getPlayer(qp.UniqueId), F.main(getName(), "Queue pause status: " + F.elem(broadcast.Paused)));
					}
				});
			});
		});
		_messenger.registerListener(QueueDeleteMessage.class, (delete, origin) ->
		{
			runSync(() ->
			{
				GetValues().forEach(qp ->
				{
					if (qp.TargetServer != null && qp.TargetServer.equals(delete.ServerName))
					{
						UtilPlayer.message(Bukkit.getPlayer(qp.UniqueId), F.main(getName(), "Queue deleted."));
					}
					qp.Queued = false;
					qp.QueuePosition = 0;
					qp.TargetServer = null;
				});
				_queueData.remove(delete.ServerName);
			});
		});
		addCommand(new CommandBase<HubQueueManager>(this, Perm.TOGGLE_QUEUE_PAUSE, "pausequeue")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /pausequeue <Server>"));
					return;
				}
				ServerInfo info = Managers.get(ClansTransferManager.class).getServer(args[0]);
				if (info != null)
				{
					ClansQueueData data = getData(info);
					if (data != null)
					{
						QueuePauseUpdateMessage message = new QueuePauseUpdateMessage();
						message.ServerName = data.ServerName;
						message.Paused = !data.QueuePaused;
						_messenger.transmitMessage(message, QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
						UtilPlayer.message(caller, F.main(getName(), "Toggling queue pause"));
						return;
					}
				}
				UtilPlayer.message(caller, F.main(getName(), "Queue not found"));
			}
		});
		addCommand(new CommandBase<HubQueueManager>(this, Perm.LIST_QUEUES, "listqueues")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				StringBuilder queues = new StringBuilder("Queues: [");
				queues.append(_queueData.values().stream().map(data -> data.ServerName).collect(Collectors.joining(", ")));
				queues.append(']');
				UtilPlayer.message(caller, F.main(getName(), queues.toString()));
			}
		});
	}
	
	private void generatePermissions()
	{
		for (QueuePriority priority : QueuePriority.values())
		{
			priority.getGranted().forEach(group -> group.setPermission(priority, true, true));
		}
		PermissionGroup.ADMIN.setPermission(Perm.JOIN_PAUSED_QUEUE, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.LIST_QUEUES, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TOGGLE_QUEUE_PAUSE, true, true);
	}
	
	public QueuePriority getHighestPriority(Player player)
	{
		Optional<QueuePriority> opt = Stream.of(QueuePriority.values()).filter(_clientManager.Get(player)::hasPermission).sorted(_prioritySorter).findFirst();
		
		if (opt.isPresent())
		{
			return opt.get();
		}
		
		return QueuePriority.DEFAULT;
	}
	
	public ClansQueueData getData(ServerInfo info)
	{
		return _queueData.get(info.Name);
	}
	
	public void attemptEnterQueue(Player player, ClansQueueData data)
	{
		if (Get(player).TargetServer != null)
		{
			if (Get(player).Queued)
			{
				UtilPlayer.message(player, F.main(getName(), "You are already in a queue!"));
			}
			else
			{
				UtilPlayer.message(player, F.main(getName(), "You are already entering a queue!"));
			}
			return;
		}
		if (data.QueuePaused && !_clientManager.Get(player).hasPermission(Perm.JOIN_PAUSED_QUEUE))
		{
			UtilPlayer.message(player, F.main(getName(), "That queue is paused and cannot currently be joined!"));
			return;
		}
		Get(player).TargetServer = data.ServerName;
		_punish.loadClient(player.getUniqueId(), client ->
		{
			if (client.isBanned())
			{
				Get(player).TargetServer = null;
				String time = UtilTime.convertString(client.getLongestBan().getTimeLeft(), 0, TimeUnit.FIT);

				if (client.getLongestBan().isPermanent())
				{
					time = "Permanent";
				}

				String reason = C.cRedB + "You are banned from Clans for " + time +
						"\n" + C.cWhite + client.getLongestBan().getReason();
				UtilPlayer.message(player, reason);
			}
			else
			{
				QueuePriority priority = getHighestPriority(player);
				PlayerJoinQueueMessage message = new PlayerJoinQueueMessage();
				message.PlayerUUID = player.getUniqueId();
				message.TargetServer = data.ServerName;
				message.PlayerPriority = priority.getWeight();
				_messenger.transmitMessage(message, QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
				UtilPlayer.message(player, F.main(getName(), "Joining queue..."));
			}
		});
	}
	
	public void leaveQueue(Player player, boolean informFailure)
	{
		if (!Get(player).Queued)
		{
			if (informFailure)
			{
				UtilPlayer.message(player, F.main(getName(), "You are not part of a queue!"));
			}
			return;
		}
		PlayerLeaveQueueMessage message = new PlayerLeaveQueueMessage();
		message.PlayerUUID = player.getUniqueId();
		message.TargetServer = Get(player).TargetServer;
		_messenger.transmitMessage(message, QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
		Get(player).TargetServer = null;
		Get(player).QueuePosition = 0;
		Get(player).Queued = false;
		UtilPlayer.message(player, F.main(getName(), "You have left the queue for " + F.elem(message.TargetServer) + "!"));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		leaveQueue(event.getPlayer(), false);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTransfer(ServerTransferEvent event)
	{
		leaveQueue(event.getPlayer(), false);
	}

	@Override
	protected QueuePlayerData addPlayer(UUID uuid)
	{
		return new QueuePlayerData(uuid);
	}
}