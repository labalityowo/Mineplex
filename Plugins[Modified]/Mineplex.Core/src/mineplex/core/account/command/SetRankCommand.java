package mineplex.core.account.command;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.OnlinePrimaryGroupUpdateEvent;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

public class SetRankCommand extends CommandBase<CoreClientManager>
{
	public SetRankCommand(CoreClientManager plugin)
	{
		super(plugin, CoreClientManager.Perm.SET_RANK_COMMAND, "set");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: /rank " + _aliasUsed + " <Player> <Rank>"));
			return;
		}
		final String target = args[0];
		Optional<PermissionGroup> maybeGroup = PermissionGroup.getGroup(args[1]);
		if (!maybeGroup.isPresent())
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Rank " + F.elem(args[1]) + " does not exist!"));
			return;
		}

		PermissionGroup group = maybeGroup.get();
		if (!group.canBePrimary())
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That rank cannot be primary!"));
			return;
		}
		Plugin.runAsync(() ->
		{
			UUID uuid = Plugin.loadUUIDFromDB(target);
			Plugin.runSync(() ->
			{
				if (uuid == null)
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), F.elem(target) + " was not found!"));
				}
				else
				{
					if (Bukkit.getPlayer(uuid) != null)
					{
						final CoreClient client = Plugin.Get(uuid);
						Plugin.setPrimaryGroup(client.getAccountId(), group, () ->
						{
							PermissionGroup old = client.getPrimaryGroup();
							client.setPrimaryGroup(group);
							UtilServer.CallEvent(new OnlinePrimaryGroupUpdateEvent(Bukkit.getPlayer(uuid), old, client.getPrimaryGroup()));
							UtilPlayer.message(Bukkit.getPlayer(uuid), F.main(Plugin.getName(), "Your rank has been updated to " + F.elem(group.name().toLowerCase()) + "!"));
							UtilPlayer.message(caller, F.main(Plugin.getName(), "You have updated " + F.elem(target + "'s") + " rank to " + F.elem(group.name().toLowerCase()) + "!"));
						});
					}
					else
					{
						Plugin.loadAccountIdFromUUID(uuid, id ->
						{
							Plugin.runSync(() ->
							{
								if (id == -1)
								{
									UtilPlayer.message(caller, F.main(Plugin.getName(), F.elem(target) + " was not found!"));
								}
								else
								{
									Plugin.setPrimaryGroup(id, group, () ->
									{
										UtilPlayer.message(caller, F.main(Plugin.getName(), "You have updated " + F.elem(target + "'s") + " rank to " + F.elem(group.name().toLowerCase()) + "!"));
									});
								}
							});
						});
					}
				}
			});
		});
	}
}