package mineplex.core.chatsnap;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.chat.ChatChannel;
import mineplex.core.chat.event.FormatPlayerChatEvent;
import mineplex.core.chatsnap.command.ChatSnapCommand;
import mineplex.core.message.PrivateMessageEvent;
import mineplex.core.punish.Punish;

/**
 * Starter class for all snapshot related functions (ie capturing messages, retrieving snapshots).
 */
public class SnapshotPlugin extends MiniPlugin
{
	public enum Perm implements Permission
	{
		CHAT_SNAP_COMMAND,
	}

	private final SnapshotManager _snapshotManager;
	private final CoreClientManager _clientManager;
	private final Punish _punish;

	public SnapshotPlugin(JavaPlugin plugin, SnapshotManager snapshotManager, CoreClientManager clientManager)
	{
		super("ChatSnap", plugin);
		_snapshotManager = snapshotManager;
		_clientManager = clientManager;
		_punish = require(Punish.class);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.TITAN.setPermission(Perm.CHAT_SNAP_COMMAND, true, true);
	}

	public SnapshotManager getSnapshotManager()
	{
		return _snapshotManager;
	}

	@Override
	public void addCommands()
	{
		addCommand(new ChatSnapCommand(this));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(FormatPlayerChatEvent e)
	{
		if (_punish.GetClient(e.getPlayer().getName()).IsMuted())
		{
			return;
		}

		_snapshotManager.cacheMessage(createSnapshot(e));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPrivateMessage(PrivateMessageEvent e)
	{
		_snapshotManager.cacheMessage(createSnapshot(e));
	}

	public Set<Integer> getAccountIds(Set<Player> players)
	{
		return players.stream().map(_clientManager::getAccountId).collect(Collectors.toSet());
	}

	public SnapshotMessage createSnapshot(FormatPlayerChatEvent e)
	{
		MessageType messageType = MessageType.CHAT;
		int senderId = _clientManager.getAccountId(e.getPlayer());
		Set<Integer> recipientIds = getAccountIds(e.getRecipients());
		recipientIds.remove(senderId);

		if (e.getChatChannel() == ChatChannel.PARTY)
		{
			messageType = MessageType.PARTY;
		}

		return new SnapshotMessage(messageType, senderId, recipientIds, e.getMessage());
	}

	public SnapshotMessage createSnapshot(PrivateMessageEvent e)
	{
		int senderId = _clientManager.getAccountId(e.getSender());
		int recipientId = _clientManager.getAccountId(e.getRecipient());
		String message = e.getMessage();
		return new SnapshotMessage(senderId, recipientId, message);
	}
}