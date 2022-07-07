package mineplex.core.communities.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.CommunityManager;

public class CommunityCreateCommand extends CommandBase<CommunityManager>
{
	public CommunityCreateCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.OWN_COMMUNITY, "create");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.help("/com create <name>", "Creates a new community", ChatColor.DARK_AQUA));
			return;
		}

		if (Plugin.Get(caller).ownsCommunity())
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You already own a community!"));
			return;
		}

		String communityName = args[0];

		if (!Plugin.isNameValid(communityName))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "A community name cannot be longer than 15 characters and must be alphanumeric!"));
			return;
		}

		if (Plugin.BLOCKED_NAMES.contains(communityName.toLowerCase()))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That name is not allowed!"));
			return;
		}

		final int accountId = Managers.get(CoreClientManager.class).getAccountId(caller);
		final String senderName = Managers.get(CoreClientManager.class).Get(caller).getName();

		Plugin.runAsync(() ->
		{
			Plugin.communityExists(communityName, (exists) ->
			{
				if (exists)
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), "A community with that name already exists!"));
					return;
				}

				if (!Plugin.isNameAllowed(caller, communityName))
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), "That name is not allowed!"));
				}
				else
				{
					Plugin.runSync(() -> Plugin.handleCreate(caller, senderName, accountId, communityName));
				}
			});
		});
	}
}