package mineplex.core.party.command.cli;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.InviteData;
import mineplex.core.party.PartyManager;

public class PartyAcceptCommand extends CommandBase<PartyManager>
{
	public PartyAcceptCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "accept", "a");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Party", "Oops. You didn't specify whose invite you're accepting!"));
			return;
		}

		Plugin.acceptInviteBySender(caller, args[0]);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (sender instanceof Player)
		{
			if (args.length == 1)
			{
				Player player = (Player) sender;

				return getMatches(args[0], Plugin.getInviteManager().getAllInvites(player).stream().map(InviteData::getInviterName));
			}
		}

		return null;
	}
}