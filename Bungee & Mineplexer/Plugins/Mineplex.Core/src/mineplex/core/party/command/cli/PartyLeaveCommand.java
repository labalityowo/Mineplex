package mineplex.core.party.command.cli;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.party.PartyManager;

public class PartyLeaveCommand extends CommandBase<PartyManager>
{
	public PartyLeaveCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "leave", "l");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.leaveParty(caller);
	}
}