package mineplex.core.newnpc.command;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.NewNPCManager.Perm;
import mineplex.core.recharge.Recharge;

public class NPCDeleteCommand extends CommandBase<NewNPCManager>
{

	public static final String RECHARGE_KEY = "Delete NPC";

	NPCDeleteCommand(NewNPCManager plugin)
	{
		super(plugin, Perm.DELETE_NPC_COMMAND, "delete");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Punch the NPC you wish to remove. You will have " + F.time("10 seconds") + " to do so."));
		Recharge.Instance.useForce(caller, RECHARGE_KEY, TimeUnit.SECONDS.toMillis(10));
	}
}
