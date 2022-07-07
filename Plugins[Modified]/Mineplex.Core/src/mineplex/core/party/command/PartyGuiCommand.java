package mineplex.core.party.command;

import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.command.gui.PartyGUIAcceptInviteCommand;
import mineplex.core.party.command.gui.PartyGUIDenyInviteCommand;
import mineplex.core.party.command.gui.PartyGUIInviteCommand;
import mineplex.core.party.command.gui.PartyGUILeaveCommand;
import mineplex.core.party.command.gui.PartyOpenInviteMenuCommand;
import mineplex.core.party.ui.menus.PartyMainMenu;
import mineplex.core.party.ui.menus.PartyOwnerMenu;
import mineplex.core.party.ui.menus.PartyViewMenu;

public class PartyGuiCommand extends MultiCommandBase<PartyManager>
{
	public PartyGuiCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "gui", "g");

		AddCommand(new PartyOpenInviteMenuCommand(plugin));
		AddCommand(new PartyGUIAcceptInviteCommand(plugin));
		AddCommand(new PartyGUIDenyInviteCommand(plugin));
		AddCommand(new PartyGUIInviteCommand(plugin));
		AddCommand(new PartyGUILeaveCommand(plugin));
	}

	// a hacky method for a hacky original system
	@Override
	protected void Help(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Party party = Plugin.getPartyByPlayer(caller);
			if (party == null)
			{
				new PartyMainMenu(Plugin).open(caller);
			}
			else if (party.getOwnerName().equalsIgnoreCase(caller.getName()))
			{
				new PartyOwnerMenu(party, Plugin).open(caller);
			}
			else
			{
				new PartyViewMenu(party, Plugin).open(caller);
			}
		}
		else if (args.length == 1)
		{
			Plugin.invite(caller, args[0]);
		}
	}
}