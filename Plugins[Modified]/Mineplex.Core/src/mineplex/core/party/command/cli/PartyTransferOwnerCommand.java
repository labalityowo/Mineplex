package mineplex.core.party.command.cli;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.Lang;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;

public class PartyTransferOwnerCommand extends CommandBase<PartyManager>
{
	public PartyTransferOwnerCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "transfer", "tr");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Party playerParty = Plugin.getPartyByPlayer(caller);

		if (playerParty == null)
		{
			UtilPlayer.message(caller, F.main("Party", "Oops. You're not in a party!"));
			return;
		}

		if (!playerParty.getOwner().getId().equals(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Party", "Oops. You're not the owner of the party!"));
			return;
		}

		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Party", "Oops. You didn't specify who you're transferring the party to!"));
			return;
		}

		Player player = Bukkit.getPlayer(args[0]);
		if (player == null)
		{
			UtilPlayer.message(caller, F.main("Party", "Could not find " + F.elem(args[0]) + "!"));
			return;
		}

		if (!playerParty.isMember(player))
		{
			UtilPlayer.message(caller, F.main("Party", "Oops. " + F.elem(player.getName())+  " is not in your party!"));
			return;
		}

		if (player == caller)
		{
			UtilPlayer.message(caller, F.main("Party", "You can't promote yourself!"));
			return;
		}

		playerParty.setOwner(player);
		Lang.TRANSFER_OWNER.send(playerParty, caller.getName(), player.getName());
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