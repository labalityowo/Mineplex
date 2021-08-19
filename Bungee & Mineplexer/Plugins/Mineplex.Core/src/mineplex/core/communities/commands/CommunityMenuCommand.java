package mineplex.core.communities.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.gui.pages.CommunityOverviewPage;

public class CommunityMenuCommand extends CommandBase<CommunityManager>
{
	public CommunityMenuCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_MENU_COMMAND, "menu");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		new CommunityOverviewPage(caller);
	}
}