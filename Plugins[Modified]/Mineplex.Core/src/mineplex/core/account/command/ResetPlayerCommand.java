package mineplex.core.account.command;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.OnlineGroupRemoveEvent;
import mineplex.core.account.event.OnlinePrimaryGroupUpdateEvent;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

public class ResetPlayerCommand extends CommandBase<CoreClientManager>
{
	public ResetPlayerCommand(CoreClientManager plugin)
	{
		super(plugin, CoreClientManager.Perm.RESET_PLAYER_COMMAND, "clear");
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
						Plugin.clearGroups(client.getAccountId(), success ->
						{
							if (success)
							{
								PermissionGroup old = client.getPrimaryGroup();
								client.setPrimaryGroup(PermissionGroup.PLAYER);
								Set<PermissionGroup> cleared = Sets.newHashSet(client.getAdditionalGroups());
								client.getAdditionalGroups().clear();
								UtilServer.CallEvent(new OnlinePrimaryGroupUpdateEvent(Bukkit.getPlayer(uuid), old, client.getPrimaryGroup()));
								for (PermissionGroup clr : cleared)
								{
									UtilServer.CallEvent(new OnlineGroupRemoveEvent(Bukkit.getPlayer(uuid), clr));
								}
								UtilPlayer.message(Bukkit.getPlayer(uuid), F.main(Plugin.getName(), "Your ranks have been cleared!"));
								UtilPlayer.message(caller, F.main(Plugin.getName(), "You have cleared " + F.elem(target + "'s") + " ranks!"));
							}
							else
							{
								UtilPlayer.message(caller, F.main(Plugin.getName(), "An error occurred while clearing " + F.elem(target) + "'s ranks!"));
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
									Plugin.clearGroups(id, success ->
									{
										if (success)
										{
											UtilPlayer.message(caller, F.main(Plugin.getName(), "You have cleared " + F.elem(target + "'s") + " ranks!"));
										}
										else
										{
											UtilPlayer.message(caller, F.main(Plugin.getName(), "An error occurred while clearing " + F.elem(target) + "'s ranks!"));
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
