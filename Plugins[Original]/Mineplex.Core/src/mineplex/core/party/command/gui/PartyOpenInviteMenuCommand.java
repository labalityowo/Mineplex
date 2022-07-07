package mineplex.core.party.command.gui;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.PartyInvitesMenu;

public class PartyOpenInviteMenuCommand extends CommandBase<PartyManager>
{
	public PartyOpenInviteMenuCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "openinvitesmenu", "invitesmenu", "im", "invites", "is");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		new PartyInvitesMenu(Plugin).open(caller);
	}
}