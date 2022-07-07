package mineplex.core.party.command.cli;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;

public class PartyInviteCommand extends CommandBase<PartyManager>
{
	public PartyInviteCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "invite", "i");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Party", "Oops. You didn't specify who to invite!"));
			return;
		}
		Plugin.invite(caller, args[0]);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			Party party = Plugin.getPartyByPlayer(player);

			return tabCompletePlayerNames(sender, args, other -> party == null || !party.isMember(other));
		}

		return null;
	}
}