package mineplex.core.party.command.cli;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;

public class PartyKickCommand extends CommandBase<PartyManager>
{
	public PartyKickCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "kick", "k");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Party party = Plugin.getPartyByPlayer(caller);
		if (party == null)
		{
			UtilPlayer.message(caller, F.main("Party", "You are not in a party!"));
			return;
		}
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Party", "I didn't quite catch that. Who are you kicking?"));
			return;
		}

		if (party.getOwnerName().equals(caller.getName()))
		{
			Plugin.kickPlayer(caller, args[0]);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			Party party = Plugin.getPartyByPlayer(player);

			if (party != null && party.isOwner(player))
			{
				return tabCompletePlayerNames(sender, args, other -> other != player && party.isMember(other));
			}
		}

		return null;
	}
}