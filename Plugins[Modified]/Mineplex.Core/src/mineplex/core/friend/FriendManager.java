package mineplex.core.friend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.friend.command.AddFriend;
import mineplex.core.friend.command.DeleteFriend;
import mineplex.core.friend.command.FriendFavouriteCommand;
import mineplex.core.friend.command.FriendVisibilityCommand;
import mineplex.core.friend.command.FriendsDisplay;
import mineplex.core.friend.data.FriendRepository;
import mineplex.core.friend.data.FriendStatus;
import mineplex.core.friend.redis.FriendAddMessage;
import mineplex.core.friend.ui.FriendShop;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.preferences.UserPreferences;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.commands.ServerCommandManager;
import mineplex.serverdata.data.PlayerStatus;

@ReflectivelyCreateMiniPlugin
public class FriendManager extends MiniDbClientPlugin<List<FriendStatus>>
{

	public enum Perm implements Permission
	{
		FRIEND_COMMAND,
		JOIN_STAFF
	}

	private static final int MAX_FRIENDS = 1000;
	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(5);
	private static final Comparator<FriendStatus> FRIEND_SORTER = (o1, o2) ->
	{
		// If not mutual
		if (o1.Status != FriendStatusType.Accepted || o2.Status != FriendStatusType.Accepted)
		{
			return o1.Name.compareToIgnoreCase(o2.Name);
		}

		// Online
		if (o1.isOnline() && !o2.isOnline())
		{
			return -1;
		}
		else if (!o1.isOnline() && o2.isOnline())
		{
			return 1;
		}

		// Favourite
		if (o1.Favourite && !o2.Favourite)
		{
			return -1;
		}
		else if (!o1.Favourite && o2.Favourite)
		{
			return 1;
		}

		// Name
		return o1.Name.compareToIgnoreCase(o2.Name);
	};

	public static Comparator<FriendStatus> getFriendSorter()
	{
		return FRIEND_SORTER;
	}

	private static final int FRIENDS_IN_CHAT = 20;

	private final DonationManager _donationManager;
	private final PreferencesManager _preferenceManager;
	private final Portal _portal;

	private final FriendRepository _repository;
	private final FriendShop _shop;

	private final Map<UUID, FriendVisibility> _visibility;

	private FriendManager()
	{
		super("Friends");

		_donationManager = require(DonationManager.class);
		_preferenceManager = require(PreferencesManager.class);
		_portal = require(Portal.class);

		_repository = new FriendRepository();
		_shop = new FriendShop(this);

		_visibility = new HashMap<>();

		generatePermissions();

		ServerCommandManager.getInstance().registerCommandType(FriendAddMessage.class, command ->
		{
			Player target = UtilPlayer.searchExact(command.getTarget());

			if (target == null)
			{
				return;
			}

			for (FriendStatus status : Get(target))
			{
				if (status.Name.equals(command.getAccepter()))
				{
					target.sendMessage(F.main(getName(), F.name(status.Name) + " has accepted your friend request."));
					return;
				}
			}

			onFriendAdd(target, command.getAccepter());
		});

		ClientManager.addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return FriendManager.this.getName() + " Visibility";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				if (resultSet.next())
				{
					byte visibility = resultSet.getByte("status");

					if (visibility > 0 && visibility < FriendVisibility.values().length)
					{
						_visibility.put(uuid, FriendVisibility.values()[visibility]);
					}
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT accountFriendData.status FROM accountFriendData WHERE accountId=" + accountId + ";";
			}
		});
	}

	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.FRIEND_COMMAND, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.JOIN_STAFF, true, true);
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public Portal getPortal()
	{
		return _portal;
	}

	public FriendVisibility getVisibility(Player player)
	{
		return _visibility.get(player.getUniqueId());
	}

	@Override
	public void addCommands()
	{
		addCommand(new AddFriend(this));
		addCommand(new DeleteFriend(this));
		addCommand(new FriendsDisplay(this));
		addCommand(new FriendVisibilityCommand(this));
		addCommand(new FriendFavouriteCommand(this));
	}

	@Override
	protected List<FriendStatus> addPlayer(UUID uuid)
	{
		return Collections.emptyList();
	}

	@Override
	public void saveData(String name, UUID uuid, int accountId)
	{
		_visibility.remove(uuid);
	}

	@EventHandler
	public void updateFriends(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || UtilServer.getPlayersCollection().isEmpty())
		{
			return;
		}

		final Player[] onlinePlayers = UtilServer.getPlayers();

		runAsync(() ->
		{
			final Map<String, Set<FriendStatus>> newData = _repository.getFriendsForAll(onlinePlayers);

			runSync(() ->
			{
				for (Player player : onlinePlayers)
				{
					List<FriendStatus> playerData = Get(player);
					Set<FriendStatus> newPlayerData = newData.get(player.getUniqueId().toString());

					if (newPlayerData != null)
					{
						List<UUID> online = playerData.stream()
								.filter(FriendStatus::isOnline)
								.map(friendStatus -> friendStatus.UUID)
								.collect(Collectors.toList());
						List<UUID> offline = playerData.stream()
								.filter(friendStatus -> !friendStatus.isOnline())
								.map(friendStatus -> friendStatus.UUID)
								.collect(Collectors.toList());

						newPlayerData.forEach(friendStatus ->
						{
							if (!friendStatus.Favourite)
							{
								return;
							}

							// Offline -> Online
							if (friendStatus.isOnline() && offline.contains(friendStatus.UUID))
							{
								player.sendMessage(F.main(getName(), F.name(friendStatus.Name) + " joined the network."));
							}
							// Online -> Offline
							else if (!friendStatus.isOnline() && online.contains(friendStatus.UUID))
							{
								player.sendMessage(F.main(getName(), F.name(friendStatus.Name) + " left the network."));
							}
						});

						Set(player, new LinkedList<>(newPlayerData));
					}
					else
					{
						playerData.clear();
					}
				}

				_shop.updatePages();
			});
		});
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (!_preferenceManager.get(player).isActive(Preference.PENDING_FRIEND_REQUESTS))
		{
			return;
		}

		int pending = 0;

		for (FriendStatus status : Get(event.getPlayer()))
		{
			if (status.Status == FriendStatusType.Pending)
			{
				pending++;
			}
		}

		if (pending > 0)
		{
			player.spigot().sendMessage(new ComponentBuilder(F.main(getName(), "You have " + F.count(pending) + " pending friend request" + (pending == 1 ? "" : "s") + "."))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(C.cGray + "Click to view your requests.").create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + AddFriend.COMMAND))
					.create());
		}
	}

	public void addFriend(final Player caller, final String name)
	{
		if (!Recharge.Instance.use(caller, "Friend " + name, COOLDOWN, false, false))
		{
			return;
		}

		if (caller.getName().equalsIgnoreCase(name))
		{
			caller.sendMessage(F.main(getName(), "You cannot add yourself as a friend"));
			return;
		}

		List<FriendStatus> statuses = Get(caller);

		if (statuses.stream().filter(s -> s.Status == FriendStatusType.Accepted || s.Status == FriendStatusType.Sent).count() >= MAX_FRIENDS)
		{
			caller.sendMessage(F.main(getName(), "You have reached the maximum amount of friends."));
			return;
		}

		boolean update = false;

		for (FriendStatus status : statuses)
		{
			if (status.Name.equalsIgnoreCase(name))
			{
				if (status.Status == FriendStatusType.Pending || status.Status == FriendStatusType.Blocked)
				{
					update = true;
					break;
				}
				else if (status.Status == FriendStatusType.Denied)
				{
					caller.sendMessage(F.main(getName(), F.name(name) + " has denied your friend request."));
					return;
				}
				else if (status.Status == FriendStatusType.Accepted)
				{
					caller.sendMessage(F.main(getName(), "You are already friends with " + F.name(name)));
					return;
				}
				else if (status.Status == FriendStatusType.Sent)
				{
					caller.sendMessage(F.main(getName(), F.name(name) + " has yet to respond to your friend request."));
					return;
				}
			}
		}

		final boolean updateFinal = update;

		runAsync(() ->
		{
			if (updateFinal)
			{
				String statusType = FriendStatusType.Accepted.toString();

				_repository.updateFriend(caller.getName(), name, statusType);
				_repository.updateFriend(name, caller.getName(), statusType);

				runSync(() ->
				{
					for (FriendStatus status : Get(caller))
					{
						if (status.Name.equalsIgnoreCase(name))
						{
							status.Status = FriendStatusType.Accepted;
							break;
						}
					}
				});
			}
			else
			{
				_repository.addFriend(caller, name);

				runSync(() ->
				{
					for (FriendStatus status : Get(caller))
					{
						if (status.Name.equalsIgnoreCase(name))
						{
							status.Status = FriendStatusType.Sent;
							break;
						}
					}
				});
			}

			new FriendAddMessage(caller.getName(), name).publish();

			runSync(() ->
			{
				if (updateFinal)
				{
					caller.sendMessage(F.main(getName(), "You and " + F.name(name) + " are now friends!"));
				}
				else
				{
					caller.sendMessage(F.main(getName(), "Added " + F.name(name) + " to your friends list!"));
				}
			});
		});
	}

	public void removeFriend(final Player caller, final String name)
	{
		if (!Recharge.Instance.use(caller, "Friend " + name, COOLDOWN, false, false))
		{
			return;
		}

		runAsync(() ->
		{
			_repository.removeFriend(caller.getName(), name);
			_repository.removeFriend(name, caller.getName());

			runSync(() ->
			{
				for (FriendStatus status : Get(caller))
				{
					if (status.Name.equalsIgnoreCase(name))
					{
						status.Status = FriendStatusType.Blocked;
						break;
					}
				}

				caller.sendMessage(F.main(getName(), "Deleted " + F.name(name) + " from your friends list!"));
			});
		});
	}

	public void setVisibility(Player caller, FriendVisibility visibility)
	{
		int accountId = ClientManager.getAccountId(caller);

		runAsync(() ->
		{
			if (_repository.updateVisibility(accountId, visibility))
			{
				runSync(() -> caller.sendMessage(F.main(getName(), "Updated your friend status to " + F.elem(visibility.getName()) + ".")));
			}
		});
	}

	public void toggleFavourite(Player caller, String target, Runnable onSuccess)
	{
		if (!Recharge.Instance.use(caller, "Friend " + target, COOLDOWN, false, false))
		{
			return;
		}

		for (FriendStatus status : Get(caller))
		{
			if (status.Name.equals(target))
			{
				runAsync(() ->
				{
					if (_repository.updateFavourite(caller.getName(), status.Name, !status.Favourite))
					{
						runSync(() ->
						{
							caller.sendMessage(F.main(getName(), F.name(status.Name) + " is " + (status.Favourite ? "no longer" : "now") + " on your favorite friends."));

							if (onSuccess != null)
							{
								onSuccess.run();
							}
						});
					}
				});

				return;
			}
		}
	}

	public void onFriendAdd(Player viewer, String friend)
	{
		if (!_preferenceManager.get(viewer).isActive(Preference.PENDING_FRIEND_REQUESTS))
		{
			return;
		}

		TextComponent message = new TextComponent("");

		TextComponent text = new TextComponent(F.main(getName(), F.name(friend) + " send you a friend request! "));

		TextComponent accept = new TextComponent("ACCEPT");
		accept.setColor(ChatColor.GREEN);
		accept.setBold(true);
		accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Accept " + friend + "'s friendship request")
				.create()));
		accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + AddFriend.COMMAND + " " + friend));

		TextComponent deny = new TextComponent("DENY");
		deny.setColor(ChatColor.RED);
		deny.setBold(true);
		deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Deny " + friend + "'s friendship request")
				.create()));
		deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + DeleteFriend.COMMAND + " " + friend));

		TextComponent exclamation = new TextComponent("!");
		exclamation.setColor(ChatColor.GRAY);

		message.addExtra(text);
		message.addExtra(accept);
		message.addExtra(" ");
		message.addExtra(deny);
		message.addExtra(exclamation);

		viewer.spigot().sendMessage(message);
	}

	public void showFriends(Player player, boolean toggle)
	{
		UserPreferences preferences = _preferenceManager.get(player);

		if (toggle)
		{
			preferences.toggle(Preference.FRIENDS_DISPLAY_INVENTORY_UI);
			_preferenceManager.save(preferences);
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.6f);
		}

		if (preferences.isActive(Preference.FRIENDS_DISPLAY_INVENTORY_UI))
		{
			showFriendsInUI(player);
		}
		else
		{
			showFriendsInChat(player);
		}
	}

	public void showFriendsInUI(Player player)
	{
		_shop.attemptShopOpen(player);
	}

	public void showFriendsInChat(Player caller)
	{
		boolean isStaff = ClientManager.Get(caller).hasPermission(Perm.JOIN_STAFF);
		boolean showPending = _preferenceManager.get(caller).isActive(Preference.PENDING_FRIEND_REQUESTS);

		List<FriendStatus> friendStatuses = Get(caller);
		friendStatuses.sort(getFriendSorter().reversed());

		boolean compress = friendStatuses.size() > FRIENDS_IN_CHAT;

		caller.sendMessage(C.cAqua + C.Strike + "======================[" + C.cWhiteB + "Friends" + C.cAqua + C.Strike + "]======================");

		if (compress)
		{
			int sent = 0, pending = 0, offline = 0;

			for (FriendStatus friend : friendStatuses)
			{
				switch (friend.Status)
				{
					case Sent:
						sent++;
						break;
					case Pending:
						pending++;
						break;
					case Accepted:
						if (!friend.isOnline())
						{
							offline++;
						}
						break;
				}
			}

			TextComponent compressed = new TextComponent(C.cGray + "Sent: " + F.count(sent) + " Pending: " + F.count(pending) + " Offline: " + F.count(offline));
			compressed.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Shows all friends.")
					.create()));
			compressed.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + FriendsDisplay.COMMAND + " chest"));
			caller.spigot().sendMessage(compressed);
		}

		// Use a LinkedHashMap so we maintain insertion order
		Map<String, TextComponent> messages = new LinkedHashMap<>();
		String joinCommand = "/server ", friendCommand = "/" + AddFriend.COMMAND + " ", unfriendCommand = "/" + DeleteFriend.COMMAND + " ";
		String favouriteCommand = "/" + FriendFavouriteCommand.COMMAND + " ", visiblityCommand = "/" + FriendVisibilityCommand.COMMAND + " ";

		for (FriendStatus friend : friendStatuses)
		{
			FriendStatusType type = friend.Status;

			if (compress)
			{
				if (type != FriendStatusType.Accepted || !friend.isOnline())
				{
					continue;
				}
			}
			else
			{
				if (type == FriendStatusType.Blocked || type == FriendStatusType.Denied || type == FriendStatusType.Pending && !showPending)
				{
					continue;
				}
			}

			TextComponent message = new TextComponent("");
			boolean online = friend.Online && friend.Visibility != FriendVisibility.INVISIBLE;
			boolean canJoin = canJoin(friend.ServerName, isStaff) && friend.Visibility == FriendVisibility.SHOWN;
			boolean clans = isClans(friend.ServerName);

			switch (type)
			{
				case Accepted:
					if (online)
					{
						if (canJoin)
						{
							TextComponent teleport = new TextComponent("Teleport");
							teleport.setColor(ChatColor.GREEN);
							teleport.setBold(true);
							teleport.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to " + friend.Name + "'s Server")
									.create()));
							teleport.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, joinCommand + friend.ServerName));
							message.addExtra(teleport);
						}
						else
						{
							TextComponent noTeleport = new TextComponent("No Teleport");
							noTeleport.setColor(ChatColor.YELLOW);
							noTeleport.setBold(true);
							noTeleport.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("You cannot teleport to this server")
									.create()));
							message.addExtra(noTeleport);
						}

						message.addExtra(" - ");
					}

					TextComponent delete = new TextComponent("Delete");
					delete.setColor(ChatColor.RED);
					delete.setBold(true);
					delete.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Remove " + friend.Name + " from your friend list")
							.create()));
					delete.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, unfriendCommand + friend.Name));
					message.addExtra(delete);

					message.addExtra(" - ");

					TextComponent name = new TextComponent(friend.Name);

					if (friend.Favourite)
					{
						name.setColor(ChatColor.YELLOW);
					}
					else if (online)
					{
						name.setColor(ChatColor.GREEN);
					}
					else
					{
						name.setColor(ChatColor.GRAY);
					}

					name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder((friend.Favourite ? "Remove " + friend.Name + " from" : "Add " + friend.Name + " to") + " your favorite friends")
							.create()));
					name.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, favouriteCommand + friend.Name));
					message.addExtra(name);

					message.addExtra(" - ");

					if (online)
					{
						if (canJoin || clans)
						{
							TextComponent server = new TextComponent(friend.ServerName);
							server.setColor(ChatColor.DARK_GREEN);
							message.addExtra(server);
						}
						else if (friend.Visibility == FriendVisibility.PRESENCE)
						{
							TextComponent server = new TextComponent(friend.Visibility.getName());
							server.setColor(ChatColor.DARK_GREEN);
							server.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(friend.Name + " is in " + friend.Visibility.getName().toLowerCase() + " mode")
									.create()));
							message.addExtra(server);
						}
						else
						{
							TextComponent server = new TextComponent("Private Staff Server");
							server.setColor(ChatColor.YELLOW);
							message.addExtra(server);
						}
					}
					else
					{
						TextComponent offlineFor = new TextComponent("Offline for " + UtilTime.MakeStr(friend.LastSeenOnline));
						offlineFor.setColor(ChatColor.GRAY);
						message.addExtra(offlineFor);
					}

					break;
				case Pending:
					TextComponent accept = new TextComponent("Accept");
					accept.setColor(ChatColor.GREEN);
					accept.setBold(true);
					accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Accept " + friend.Name + "'s friend request")
							.create()));
					accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, friendCommand + friend.Name));
					message.addExtra(accept);

					message.addExtra(" - ");

					TextComponent deny = new TextComponent("Deny");
					deny.setColor(ChatColor.RED);
					deny.setBold(true);
					deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Deny " + friend.Name + "'s friend request")
							.create()));
					deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, unfriendCommand + friend.Name));
					message.addExtra(deny);

					message.addExtra(" - ");

					TextComponent request = new TextComponent(friend.Name + " Requested Friendship");
					request.setColor(ChatColor.GRAY);

					message.addExtra(request);

					break;
				case Sent:
					TextComponent cancel = new TextComponent("Cancel");
					cancel.setColor(ChatColor.RED);
					cancel.setBold(true);
					cancel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cancel your friend request to " + friend.Name)
							.create()));
					cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, unfriendCommand + friend.Name));
					message.addExtra(cancel);

					message.addExtra(" - ");

					TextComponent pending = new TextComponent(friend.Name + " Request Pending");
					pending.setColor(ChatColor.GRAY);

					message.addExtra(pending);
					break;
			}

			messages.put(friend.Name, message);
		}

		if (messages.isEmpty() && !compress)
		{
			caller.sendMessage(" ");
			caller.sendMessage("Welcome to your Friends List!");
			caller.sendMessage(" ");
			caller.sendMessage("To add friends, type " + C.cGreen + "/friend <Player Name>");
			caller.sendMessage(" ");
			caller.sendMessage("Type " + C.cGreen + "/friend" + C.Reset + " at any time to interact with your friends!");
			caller.sendMessage(" ");
		}
		else
		{
			messages.values().forEach(message -> caller.spigot().sendMessage(message));

			TextComponent message = new TextComponent("");

			FriendVisibility playerVisibility = getVisibility(caller);

			for (FriendVisibility visibility : FriendVisibility.values())
			{
				TextComponent vis = new TextComponent(visibility.getName().toUpperCase());
				vis.setColor(visibility.getColour());
				vis.setBold(true);
				vis.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set your visibility to " + visibility.getName())
						.create()));
				vis.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, visiblityCommand + visibility));
				vis.setUnderlined(visibility == playerVisibility);

				if (message.getExtra() != null)
				{
					message.addExtra(" - ");
				}

				message.addExtra(vis);
			}

			caller.spigot().sendMessage(message);
		}

		TextComponent toggle = new TextComponent("");

		TextComponent line = new TextComponent("======================");
		line.setColor(ChatColor.AQUA);
		line.setStrikethrough(true);

		TextComponent command = new TextComponent("Toggle GUI");
		command.setColor(ChatColor.DARK_AQUA);
		command.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Toggle friends to display in an inventory")
				.create()));
		command.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + FriendsDisplay.COMMAND));

		toggle.addExtra(line);
		toggle.addExtra(command);
		toggle.addExtra(line);

		caller.spigot().sendMessage(toggle);
	}

	public boolean canJoin(String serverName, Player player)
	{
		return canJoin(serverName, ClientManager.Get(player).hasPermission(Perm.JOIN_STAFF));
	}

	private boolean canJoin(String serverName, boolean isPlayerStaff)
	{
		if (serverName == null)
		{
			return false;
		}

		if (serverName.startsWith("Staff-"))
		{
			return isPlayerStaff;
		}

		return !serverName.startsWith("CUST-") && !isClans(serverName);
	}

	private boolean isClans(String serverName)
	{
		return serverName != null && serverName.startsWith("Clans-");
	}

	public void updatePlayerStatus(UUID playerUUID, PlayerStatus status)
	{
		_repository.updatePlayerStatus(playerUUID, status);
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(uuid, _repository.loadClientInformation(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT tA.Name, status, tA.lastLogin, now(), uuidTarget, favourite, " + FriendRepository.GET_VISIBILITY_QUERY + " FROM accountFriend INNER JOIN accounts AS fA ON fA.uuid = uuidSource INNER JOIN accounts AS tA ON tA.uuid = uuidTarget WHERE uuidSource = '" + uuid + "';";
	}
}