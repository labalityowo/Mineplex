package mineplex.core.chatsnap.command;

import java.util.Set;

import org.bukkit.entity.Player;

import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.chatsnap.SnapshotMessage;
import mineplex.core.chatsnap.SnapshotPlugin;
import mineplex.core.chatsnap.SnapshotRepository;
import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

/**
 * A command which when executed will create a chat log which will be viewable online.
 */
public class ChatSnapCommand extends CommandBase<SnapshotPlugin>
{
	public ChatSnapCommand(SnapshotPlugin plugin)
	{
		super(plugin, SnapshotPlugin.Perm.CHAT_SNAP_COMMAND, "chatsnap");
	}

	@Override
	public void Execute(Player player, String[] args)
	{
		if (args == null || args.length == 0)
		{
			SnapshotManager manager = Plugin.getSnapshotManager();
			SnapshotRepository repository = manager.getRepository();
			int accountId = _commandCenter.GetClientManager().getAccountId(player);

			Plugin.getSnapshotManager().getRepository().getUserSnapshots(accountId).thenAccept(snapshotIds ->
			{
				if (snapshotIds.size() < SnapshotManager.MAX_SNAPSHOTS)
				{
					Set<SnapshotMessage> messages = manager.getMessagesInvolving(accountId);

					repository.createSnapshot(accountId).thenAccept(snapshotMetadata ->
					{
						String token = snapshotMetadata.getToken().orElseThrow(() ->
								new IllegalStateException("Snapshot doesn't have a token."));

						repository.insertMessages(snapshotMetadata.getId(), messages).join();

						UtilPlayer.message(player, F.main(Plugin.getName(), "Snapshot successfully created."));

						new JsonMessage(F.main(Plugin.getName(), "Your snapshot token is: "))
								.extra(F.elem(token))
								.click(ClickEvent.OPEN_URL, SnapshotRepository.getURL(token))
								.sendToPlayer(player);
					});
				}
				else
				{
					UtilPlayer.message(player, F.main(Plugin.getName(),
							C.cRed + "Cannot create snapshot, you have reached the limit."));
				}
			});
		}
		else
		{
			UtilPlayer.message(player, F.main(Plugin.getName(), C.cRed + "Invalid Usage: " + F.elem("/" + _aliasUsed)));
		}
	}
}