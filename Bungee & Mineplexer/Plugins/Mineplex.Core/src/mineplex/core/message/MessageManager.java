package mineplex.core.message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.chat.Chat;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.friend.FriendManager;
import mineplex.core.friend.FriendStatusType;
import mineplex.core.friend.data.FriendStatus;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.message.commands.AdminCommand;
import mineplex.core.message.commands.AnnounceCommand;
import mineplex.core.message.commands.MessageAdminCommand;
import mineplex.core.message.commands.MessageCommand;
import mineplex.core.message.commands.ResendAdminCommand;
import mineplex.core.message.commands.ResendCommand;
import mineplex.core.message.redis.RedisMessage;
import mineplex.core.message.redis.RedisMessageCallback;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.punish.Punish;
import mineplex.core.punish.PunishClient;
import mineplex.core.punish.Punishment;
import mineplex.core.punish.PunishmentSentence;
import mineplex.serverdata.commands.AnnouncementCommand;
import mineplex.serverdata.commands.ServerCommandManager;

public class MessageManager extends MiniClientPlugin<ClientMessage>
{
	public enum Perm implements Permission
	{
		SEE_ADMIN,
		BYPASS_INCOGNITO,
		BYPASS_SPAM,
		ADMIN_COMMAND,
		ANNOUNCE_COMMAND,
		MESSAGE_ADMIN_COMMAND,
		MESSAGE_COMMAND,
		RESEND_ADMIN_COMMAND,
		RESEND_COMMAND,
	}
	
	private CoreClientManager _clientManager;
	private FriendManager _friendsManager;
	private IgnoreManager _ignoreManager;
	private IncognitoManager _incognitoManager;
	
	private HashMap<UUID, BukkitRunnable> _messageTimeouts = new HashMap<UUID, BukkitRunnable>();
	private PreferencesManager _preferences;
	private Punish _punish;
	private Chat _chat;
	private List<String> _randomMessage;
	private String _serverName;

	public MessageManager(JavaPlugin plugin, IncognitoManager incognitoManager, CoreClientManager clientManager, PreferencesManager preferences,
			IgnoreManager ignoreManager, Punish punish, FriendManager friendManager, Chat chat)
	{
		super("Message", plugin);
		
		_incognitoManager = incognitoManager;
		_clientManager = clientManager;
		_preferences = preferences;
		_ignoreManager = ignoreManager;
		_punish = punish;
		_friendsManager = friendManager;
		_chat = chat;
		_serverName = getPlugin().getConfig().getString("serverstatus.name");

		ServerCommandManager.getInstance().registerCommandType("AnnouncementCommand", AnnouncementCommand.class, command ->
		{
			PermissionGroup group = PermissionGroup.valueOf(command.getRank());
			String message = command.getMessage();

			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (_clientManager.Get(player).getPrimaryGroup().inheritsFrom(group))
				{
					if (command.getDisplayTitle())
					{
						UtilTextMiddle.display(C.cYellow + "Announcement", message, 10, 120, 10, player);
					}

					UtilPlayer.message(player, F.main("Announcement", C.cAqua + message));
				}
			}
		});

		ServerCommandManager.getInstance().registerCommandType("RedisMessage", RedisMessage.class, this::receiveMessage);
		ServerCommandManager.getInstance().registerCommandType("RedisMessageCallback", RedisMessageCallback.class, this::receiveMessageCallback);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.TRAINEE.setPermission(Perm.BYPASS_INCOGNITO, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.BYPASS_SPAM, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.SEE_ADMIN, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.ADMIN_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.ANNOUNCE_COMMAND, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.MESSAGE_ADMIN_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.MESSAGE_COMMAND, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.RESEND_ADMIN_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.RESEND_COMMAND, true, true);
	}

	public void addCommands()
	{
		addCommand(new MessageCommand(this));
		addCommand(new ResendCommand(this));

		addCommand(new MessageAdminCommand(this));
		addCommand(new ResendAdminCommand(this));

		addCommand(new AnnounceCommand(this));
		//addCommand(new GlobalCommand(this));

		addCommand(new AdminCommand(this));
	}

	@Override
	protected ClientMessage addPlayer(UUID uuid)
	{
		Set(uuid, new ClientMessage());
		return Get(uuid);
	}

	public boolean canMessage(Player from, Player to)
	{
		if (!canSenderMessageThem(from, to.getName()))
		{
			return false;
		}

		String canMessage = canReceiverMessageThem(from.getName(), to);

		if (canMessage != null)
		{
			from.sendMessage(canMessage);

			return false;
		}

		return true;
	}

	public String canReceiverMessageThem(String sender, Player target)
	{
		// If the receiver has turned off private messaging and the sender isn't a mod
		if (!_preferences.get(target).isActive(Preference.PRIVATE_MESSAGING))
		{
			String message = C.cPurple + target.getName() + " has private messaging disabled.";

			if (GetClientManager().Get(target).hasPermission(Perm.SEE_ADMIN))
			{
				return message + " Try using /a <message> to contact them instead!";
			}

			return message;
		}

		// If the receiver is ignoring the sender, and the sender isn't a mod
		if (_ignoreManager.isIgnoring(target, sender))
		{
			return F.main(_ignoreManager.getName(), ChatColor.GRAY + "That player is ignoring you");
		}

		return null;
	}

	public boolean isMuted(Player sender)
	{
		PunishClient client = _punish.GetClient(sender.getName());

		if (client != null && client.IsMuted())
		{
			Punishment punishment = client.GetPunishment(PunishmentSentence.Mute);

			sender.sendMessage(F.main(_punish.getName(), "Shh, you're muted because "

			+ punishment.GetReason()

			+ " by "

			+ punishment.GetAdmin()

			+ " for "

			+ C.cGreen

			+ UtilTime.convertString(punishment.GetRemaining(), 1, TimeUnit.FIT) + "."));

			return true;
		}

		return false;
	}

	public boolean canSenderMessageThem(Player sender, String target)
	{
		if (isMuted(sender))
		{
			return false;
		}

		if (_ignoreManager.isIgnoring(sender, target))
		{
			sender.sendMessage(F.main(_ignoreManager.getName(), ChatColor.GRAY + "You are ignoring that player"));

			return false;
		}

		return true;
	}

	public void DoMessage(Player from, Player to, String message)
	{
		PrivateMessageEvent pmEvent = new PrivateMessageEvent(from, to, message);
		Bukkit.getServer().getPluginManager().callEvent(pmEvent);
		if (pmEvent.isCancelled())
			return;

		if (!canMessage(from, to))
		{
			return;
		}

		// My attempt at trying to mitigate some of the spam bots - Phinary
		// Triggers if they are whispering a new player
		if (!GetClientManager().Get(from).hasPermission(Perm.BYPASS_SPAM) && Get(from).LastTo != null
				&& !Get(from).LastTo.equalsIgnoreCase(to.getName()))
		{
			long delta = System.currentTimeMillis() - Get(from).LastToTime;

			if (Get(from).SpamCounter > 3 && delta < Get(from).SpamCounter * 1000)
			{
				from.sendMessage(F.main("Cooldown", "Try sending that message again in a few seconds"));
				Get(from).LastTo = to.getName();
				return;
			}
			else if (delta < 8000)
			{
				// Silently increment spam counter whenever delta is less than 8 seconds
				Get(from).SpamCounter++;
			}
		}

		// Inform
		UtilPlayer.message(from, C.cGold + "§l" + from.getName() + " > " + to.getName() + C.cYellow + " §l" + message);

		if (!_preferences.get(from).isActive(Preference.PRIVATE_MESSAGING))
		{
			UtilPlayer.message(from, C.cPurple + to.getName() + " won't be able to reply, because you have private messaging disabled.");
			UtilPlayer.message(from, C.cPurple + "You can re-enable it in your preferences.");
		}

		// Save
		Get(from).LastTo = to.getName();
		Get(from).LastToTime = System.currentTimeMillis();

		if (GetClientManager().Get(to).getRealOrDisguisedPrimaryGroup() == PermissionGroup.DEV)
		{
			UtilPlayer.message(from, "");
			UtilPlayer.message(from, C.cPurple + to.getName() + " is often AFK or minimized, due to plugin development.");
			UtilPlayer.message(from, C.cPurple + "Please be patient if they do not reply instantly.");
		}

		// Log
		// Logger().logChat("Private Message", from, to.getName(), message);

		// Sound
		from.playSound(to.getLocation(), Sound.NOTE_PIANO, 1f, 1f);
		to.playSound(to.getLocation(), Sound.NOTE_PIANO, 2f, 2f);

		// Send
		UtilPlayer.message(to, C.cGold + "§l" + from.getName() + " > " + to.getName() + C.cYellow + " §l" + message);
		if (_punish.GetClient(to.getName()).IsMuted() && _preferences.get(to).isActive(Preference.INFORM_MUTED))
		{
			UtilPlayer.message(from, F.main(getName(), F.elem(to.getName()) + " is currently muted and cannot reply to you!"));
		}
	}

	public void DoMessageAdmin(Player from, Player to, String message)
	{
		// Inform
		UtilPlayer.message(from, C.cDPurple + "-> " + _clientManager.Get(to).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true) + " " + to.getName() + " "
				+ C.cPurple + message);

		// Inform Admins
		for (Player staff : UtilServer.getPlayers())
		{
			if (!to.equals(staff) && !from.equals(staff))
			{
				if (_clientManager.Get(staff).hasPermission(Perm.SEE_ADMIN))
				{
					UtilPlayer.message(staff, _clientManager.Get(from).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true) + " " + from.getName() + C.cDPurple
							+ " -> " + _clientManager.Get(to).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true) + " " + to.getName() + " " + C.cPurple + message);
				}
			}
		}

		// Save
		Get(from).LastAdminTo = to.getName();

		// Send
		UtilPlayer.message(to, C.cDPurple + "<- " + _clientManager.Get(from).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true) + " " + from.getName() + " "
				+ C.cPurple + message);

		// Sound
		from.playSound(to.getLocation(), Sound.NOTE_PIANO, 1f, 1f);
		to.playSound(to.getLocation(), Sound.NOTE_PIANO, 2f, 2f);

		// Log XXX
		// Logger().logChat("Staff Message", from, to.getName(), message);
	}

	// Module Functions
	@Override
	public void enable()
	{
		_randomMessage = Arrays.asList(
				"The FitnessGram™ Pacer Test is a multistage aerobic capacity test that progressively gets more difficult as it continues",
				"do you feel it now mr krabs?!",
				"hisssssssss",
				"what's a leader?",
				"what guardians?",
				"rawr!",
				"where were you when the emus won the great emu war?",
				"Hello, do you have any wild boars for purchase?",
				"There's a snake in my boot!",
				"Monk, I need a Monk!",
				"Hi, I'm from planet minecraft, op me plz dooooood!",
				"Somebody's poisoned the waterhole!",
				"MORE ORBZ MORE ORBZ MORE ORBZ MORE ORBZ!",
				"Chiss is a chiss and chiss chiss.",
				"*_*",
				"#swag",
				"Everything went better then I thought.",
				"HAVE A CHICKEN!",
				"follow me, i have xrays",
				"I'm making a java",
				"Solid 2.9/10",
				"close your eyes to sleep",
				"I crashed because my internet ran out.",
				"I saw morgan freeman on a breaking bad ad on a bus.",
				"Where is the volume control?",
				"I saw you playing on youtube with that guy and stuff.",
				"Your worms must be worse than useless.",
				"meow",
				"7",
				"Don't you wish your girlfriend was hot like me?",
				"how do you play mindcrafts?",
				"7 cats meow meow meow meow meow meow meow",
				"For King Jonalon!!!!!",
				"Do you like apples?",
				"I'm Happy Happy Happy.",
				"kthxbye",
				"i like pie.",
				"Do you play Clash of Clans?",
				"Mmm...Steak!",
				"Poop! Poop everywhere!",
				"I'm so forgetful. Like I was going to say somethin...wait what were we talking about?",
				"Mmm...Steak!",
				"#BlameAlex"
		);
	}

	public CoreClientManager GetClientManager()
	{
		return _clientManager;
	}

	public String GetRandomMessage()
	{
		if (_randomMessage.isEmpty())
			return "meow";

		return UtilMath.randomElement(_randomMessage);
	}

	public void Help(Player caller)
	{
		Help(caller, null);
	}

	public void Help(Player caller, String message)
	{
		UtilPlayer.message(caller, F.main(_moduleName, ChatColor.RED + "Err...something went wrong?"));
	}

	public void receiveMessage(RedisMessage globalMessage)
	{
		Player to = Bukkit.getPlayerExact(globalMessage.getTarget());

		if (to == null)
			return;

		if (globalMessage.isStaffMessage())
		{
			// Message the receiver
			UtilPlayer.message(to, C.cDPurple + "<- " + globalMessage.getRank() + " " + globalMessage.getSender() + " "
					+ C.cPurple + globalMessage.getMessage());

			to.playSound(to.getLocation(), Sound.NOTE_PIANO, 2f, 2f);

			String toRank = _clientManager.Get(to).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true);

			// Message the sender
			RedisMessageCallback message = new RedisMessageCallback(globalMessage, true, to.getName(),

			C.cDPurple + "-> " + toRank + " " + to.getName() + " " + C.cPurple + globalMessage.getMessage(), false);

			// Inform Admins
			for (Player staff : UtilServer.getPlayers())
			{
				if (!to.equals(staff))
				{
					if (_clientManager.Get(staff).hasPermission(Perm.SEE_ADMIN))
					{
						UtilPlayer.message(staff,

								globalMessage.getRank() + " " + globalMessage.getSender() + C.cPurple + " " + message.getMessage());
					}
				}
			}

			message.publish();
		}
		else
		{
			String canMessage = canReceiverMessageThem(globalMessage.getSender(), to);

			if (canMessage != null)
			{
				RedisMessageCallback message = new RedisMessageCallback(globalMessage, false, null, canMessage, false);

				message.publish();

				return;
			}

			String message = C.cGold + "§l" + globalMessage.getSender() + " > " + to.getName() + C.cYellow + " §l"
					+ globalMessage.getMessage();

			// Message the receiver
			UtilPlayer.message(to, message);

			to.playSound(to.getLocation(), Sound.NOTE_PIANO, 2f, 2f);
			
			boolean informMuted = _punish.GetClient(to.getName()).IsMuted() && _preferences.get(to).isActive(Preference.INFORM_MUTED);

			// Message the sender
			RedisMessageCallback redisMessage = new RedisMessageCallback(globalMessage, false, to.getName(), message, informMuted);

			redisMessage.publish();
		}
	}

	public void receiveMessageCallback(RedisMessageCallback message)
	{
		BukkitRunnable runnable = _messageTimeouts.remove(message.getUUID());

		if (runnable != null)
		{
			runnable.cancel();
		}

		Player target = Bukkit.getPlayerExact(message.getTarget());

		if (target != null)
		{
			target.sendMessage(message.getMessage());
			
			if (message.informMuted())
			{
				UtilPlayer.message(target, F.main(getName(), F.elem(message.getLastReplied()) + " is currently muted and cannot reply to you!"));
			}

			target.playSound(target.getLocation(), Sound.NOTE_PIANO, 2f, 2f);

			if (message.getLastReplied() != null)
			{
				if (message.isStaffMessage())
				{
					Get(target).LastAdminTo = message.getLastReplied();
				}
				else
				{
					Get(target).LastTo = message.getLastReplied();
				}
			}

			if (message.isStaffMessage() && message.getLastReplied() != null)
			{
				String recevierRank = _clientManager.Get(target).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true);

				// Inform Admins
				for (Player staff : UtilServer.getPlayers())
				{
					if (!target.equals(staff))
					{
						if (_clientManager.Get(staff).hasPermission(Perm.SEE_ADMIN))
						{
							UtilPlayer.message(staff,

									recevierRank + " " + target.getName() + " " + C.cPurple + message.getMessage());
						}
					}
				}
			}
		}
	}

	public void sendMessage(final Player sender, final String target, final String message, final boolean isReply,
			final boolean adminMessage)
	{
		FriendStatus friend = null;
		
		if (!adminMessage)
		{
			for (FriendStatus friendInfo : _friendsManager.Get(sender))
			{
				// Don't consider them "the friend" if their request has not been accepted
				if (friendInfo.Status != FriendStatusType.Accepted)
				{
					continue;
				}

				// We don't grab this guy even if name matches because he is offline. This way, we can get a free message without
				// extra coding as we can't do anything extra with a offline friend..
				if ((isReply || friendInfo.Online) && friendInfo.Name.equalsIgnoreCase(target))
				{
					friend = friendInfo;
					break;
				}

				// If this isn't a reply, no other matches found, friend is online and name begins with param.. Our first guess.
				if (!isReply && friend == null && friendInfo.Online
						&& friendInfo.Name.toLowerCase().startsWith(target.toLowerCase()))
				{
					friend = friendInfo;
				}
			}
		}

		final FriendStatus friendInfo = friend;

		runAsync(() ->
		{
			final String newMessage = adminMessage ? message : _chat.filterMessage(sender, message);
			runSync(() -> sendMessage(sender, target, newMessage, adminMessage, isReply, friendInfo));
		});
	}

	private void sendMessage(final Player sender, String target, String message, final boolean adminMessage, boolean isReply, FriendStatus friend)
	{
		// We now have the friend object, if its not null. We are sending the message to that player.

		// Only notify player if friend is null and its not a reply
		Player to = UtilPlayer.searchOnline(sender, target, !adminMessage && friend == null && !isReply);

		// If isn't admin message, friend is null and target is null. Return because location of receiver is unknown.
		if (!adminMessage && (friend == null || !friend.Online) && to == null)
		{
			// We need to notify them that the player they are replying to is gone
			if (isReply)
			{
				UtilPlayer.message(sender, F.main(getName(), F.name(target) + " is no longer online."));
			}

			return;
		}

		// If this is a message inside the server
		if (to != null)
		{
			if (_incognitoManager.Get(to).Status && !_clientManager.Get(sender).hasPermission(Perm.BYPASS_INCOGNITO))
			{
				UtilPlayer.message(sender, F.main("Online Player Search", F.elem("0") + " matches for [" + F.elem(target) + "]."));
				return;
			}
			
			if (adminMessage)
			{
				DoMessageAdmin(sender, to, message);
			}
			else
			{
				if (_preferences.get(to).isActive(Preference.FRIEND_MESSAGES_ONLY)
						&& (friend == null || friend.Status != FriendStatusType.Accepted)
						&& !sender.equals(to))
				{
					sender.sendMessage(F.main(getName(), F.name(to.getName()) + " only allows messages from friends."));
					return;
				}

				DoMessage(sender, to, message);
			}
		}
		else
		{
			// Looks like we will be using redis to send a message

			// First get the full name of the player and make it a final String for use in a runnable
			final String playerTarget = adminMessage ? target : friend.Name;

			// If this is a admin message, or the sender isn't muted/ignoring the target
			if (adminMessage || canSenderMessageThem(sender, playerTarget))
			{
				runAsync(() ->
				{
					// TODO Newgarbo wrote this stuff inefficiently and for sake of time and thousands of players i'm going to just comment this out
					/*
					if (IncognitoManager.Instance.getRepository().GetStatus(playerTarget))
					{
						UtilPlayer.message(sender, F.main("Online Player Search", F.elem("0") + " matches for [" + F.elem(target) + "]."));
						return;
					}
					 */

					runSync(() ->
					{
						// Construct the command to send to redis
						RedisMessage globalMessage = new RedisMessage(_serverName,
								sender.getName(),
								adminMessage ? null : friend.ServerName,
								playerTarget,
								message,
								// Include the sender's rank if this is a admin message. So we can format the receivers chat.
								adminMessage ? _clientManager.Get(sender).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true) : null);

						final UUID uuid = globalMessage.getUUID();

						// A backup for the rare case where the message fails to deliver. Server doesn't respond
						BukkitRunnable runnable = new BukkitRunnable()
						{
							public void run()
							{
								_messageTimeouts.remove(uuid);

								// Inform the player that the message failed to deliver
								UtilPlayer.message(
										sender,
										F.main((adminMessage ? "Admin " : "") + "Message", C.mBody + " Failed to send message to ["
												+ C.mElem + playerTarget + C.mBody + "]."));
							}
						};

						// This will activate in 2 seconds
						runnable.runTaskLater(getPlugin(), 40);

						// The key is the UUID its trading between servers
						_messageTimeouts.put(uuid, runnable);

						// Time to send the message!
						globalMessage.publish();
					});
				});
			}
		}
	}

	public IncognitoManager getIncognitoManager()
	{
		return _incognitoManager;
	}

	public PreferencesManager getPreferences()
	{
		return _preferences;
	}
}