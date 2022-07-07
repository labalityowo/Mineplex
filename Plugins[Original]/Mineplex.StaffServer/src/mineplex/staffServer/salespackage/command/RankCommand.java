package mineplex.staffServer.salespackage.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class RankCommand extends CommandBase<SalesPackageManager>
{
	private static final List<String> ACCEPTED_RANKS = Collections.unmodifiableList(Arrays.asList("PLAYER", "ULTRA", "HERO", "LEGEND", "TITAN", "ETERNAL"));
	
	public RankCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "rank");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length != 2)
		{
			return;
		}
		
		String playerName = args[0];
		String rank = args[1].toUpperCase();
		
		if (ACCEPTED_RANKS.contains(rank))
		{
			PermissionGroup group = PermissionGroup.valueOf(rank);
			Plugin.runAsync(() ->
			{
				UUID uuid = Plugin.getClientManager().loadUUIDFromDB(playerName);
				
				Plugin.getClientManager().loadAccountIdFromUUID(uuid, id ->
				{
					Plugin.runSync(() ->
					{
						if (id == -1)
						{
							UtilPlayer.message(caller, F.main(Plugin.getName(), "Could not find " + F.elem(playerName) + "!"));
						}
						else
						{
							Plugin.getClientManager().setPrimaryGroup(id, group, () -> UtilPlayer.message(caller, F.main(Plugin.getName(), F.elem(playerName) + "'s rank has been updated to " + rank + "!")));
						}
					});
				});
			});
		}
	}
}
