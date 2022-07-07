package mineplex.core.party.command.cli;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.party.PartyManager;

public class PartyBlockCommand extends CommandBase<PartyManager>
{
	public PartyBlockCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "block", "b");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{

	}
}