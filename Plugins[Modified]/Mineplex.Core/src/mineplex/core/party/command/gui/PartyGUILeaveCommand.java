package mineplex.core.party.command.gui;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.party.PartyManager;

public class PartyGUILeaveCommand extends CommandBase<PartyManager>
{
	public PartyGUILeaveCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "leave", "l");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.leaveParty(caller);
	}
}