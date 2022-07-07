package mineplex.core.newnpc.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.NewNPCManager.Perm;

public class NPCBuildCommand extends CommandBase<NewNPCManager>
{

	NPCBuildCommand(NewNPCManager plugin)
	{
		super(plugin, Perm.BUILD_NPC_COMMAND, "builder");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.createBuilder(caller);
	}
}
