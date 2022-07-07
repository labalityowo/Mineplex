package mineplex.core.party.command.cli;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.PartyManager;

public class PartyHelpCommand extends CommandBase<PartyManager>
{
	public PartyHelpCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "help", "h");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main("Party", "Parties Help"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [help/h]") + " - Shows this help dialog"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [invite/i] [player]") + " - Invite [player] to your party"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [invites/is] (page)") + " - List your current pending invitations"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [kick/k] [player]") + " - Kick [player] from your party"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [accept/a] [player]") + " - Accept your invitation to [player]'s party"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [deny/d] [player]") + " - Deny your invitation to [player]'s party"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [leave/l]") + " - Leave your current party"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [disband/db]") + " - Disband your current party"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [transfer/tr] [player]") + " - Transfers ownership of the party to another player"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [gui/g]") + " - Opens the party GUI"));
		UtilPlayer.message(caller, F.main("Party", F.elem("/party [toggle/t]") + " - Toggles between the GUI and the chat"));
	}
}