package mineplex.core.account.command;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.OnlineGroupAddEvent;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

public class AddRankCommand extends CommandBase<CoreClientManager>
{
	public AddRankCommand(CoreClientManager plugin)
	{
		super(plugin, CoreClientManager.Perm.ADD_RANK_COMMAND, "add");
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
						Plugin.addAdditionalGroup(client.getAccountId(), maybeGroup.get(), success ->
						{
							if (success)
							{
								client.addAdditionalGroup(maybeGroup.get());
								UtilServer.CallEvent(new OnlineGroupAddEvent(Bukkit.getPlayer(uuid), group));
								UtilPlayer.message(Bukkit.getPlayer(uuid), F.main(Plugin.getName(), "You have gained sub-rank " + F.elem(group.name().toLowerCase()) + "!"));
								UtilPlayer.message(caller, F.main(Plugin.getName(), "You have added sub-rank " + F.elem(group.name().toLowerCase()) + " to " + F.elem(target) + "!"));
							}
							else
							{
								UtilPlayer.message(caller, F.main(Plugin.getName(), "An error occurred while adding sub-rank " + F.elem(group.name().toLowerCase()) + " to " + F.elem(target) + "! They may already have it."));
							}
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
									Plugin.addAdditionalGroup(id, group, success ->
									{
										if (success)
										{
											UtilPlayer.message(caller, F.main(Plugin.getName(), "You have added sub-rank " + F.elem(group.name().toLowerCase()) + " to " + F.elem(target) + "!"));
										}
										else
										{
											UtilPlayer.message(caller, F.main(Plugin.getName(), "An error occurred while adding sub-rank " + F.elem(group.name().toLowerCase()) + " to " + F.elem(target) + "! They may already have it."));
										}
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