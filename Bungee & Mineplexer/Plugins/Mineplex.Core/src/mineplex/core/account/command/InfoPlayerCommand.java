package mineplex.core.account.command;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class InfoPlayerCommand extends CommandBase<CoreClientManager>
{
	public InfoPlayerCommand(CoreClientManager plugin)
	{
		super(plugin, CoreClientManager.Perm.RANK_INFO_COMMAND, "info");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: /rank " + _aliasUsed + " <Player>"));
			return;
		}
		final String target = args[0];
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
						Plugin.fetchGroups(client.getAccountId(), (primaryGroup, additionalGroups) ->
						{
							UtilPlayer.message(caller, F.main(Plugin.getName(), "Rank Information of " + target + ":"));
							UtilPlayer.message(caller, C.cBlue + "Primary: " + C.cGray + primaryGroup.name().toLowerCase());
							UtilPlayer.message(caller, C.cBlue + "Additional (" + additionalGroups.size() + "):");
							for (PermissionGroup group : additionalGroups)
							{
								UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + group.name().toLowerCase());
							}
						}, () ->
						{
							UtilPlayer.message(caller, F.main(Plugin.getName(), "An error occurred while listing " + F.elem(target) + "'s ranks!"));
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
									Plugin.fetchGroups(id, (primaryGroup, additionalGroups) ->
									{
										UtilPlayer.message(caller, F.main(Plugin.getName(), "Rank Information of " + target + ":"));
										UtilPlayer.message(caller, C.cBlue + "Primary: " + C.cGray + primaryGroup.name().toLowerCase());
										UtilPlayer.message(caller, C.cBlue + "Additional (" + additionalGroups.size() + "):");
										for (PermissionGroup group : additionalGroups)
										{
											UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + group.name().toLowerCase());
										}
									}, () ->
									{
										UtilPlayer.message(caller, F.main(Plugin.getName(), "An error occurred while listing " + F.elem(target) + "'s ranks!"));
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