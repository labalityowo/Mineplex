package mineplex.core.party.command.cli;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.party.PartyManager;

public class PartyDisbandCommand extends CommandBase<PartyManager>
{
	public PartyDisbandCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "disband", "db");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.disband(caller);
	}
}