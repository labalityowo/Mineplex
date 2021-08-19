package mineplex.core.newnpc.command;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.NewNPCManager.Perm;
import mineplex.core.recharge.Recharge;

public class NPCClearCommand extends CommandBase<NewNPCManager>
{

	private static final String RECHARGE_KEY = "Clear NPCS";

	NPCClearCommand(NewNPCManager plugin)
	{
		super(plugin, Perm.CLEAR_NPC_COMMAND, "clear");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (Recharge.Instance.usable(caller, RECHARGE_KEY))
		{
			caller.sendMessage(F.main(Plugin.getName(), "Are you really sure you want to delete ALL NPCs? This will remove every one off every server."));
			caller.sendMessage(F.main(Plugin.getName(), "If you are sure then run " + F.elem("/npc clear") + " again within " + F.time("10 seconds")) + ".");
			Recharge.Instance.useForce(caller, RECHARGE_KEY, TimeUnit.SECONDS.toMillis(10));
			return;
		}

		caller.sendMessage(F.main(Plugin.getName(), "Deleting all NPCs..."));
		Plugin.clearNPCS(true);
		caller.sendMessage(F.main(Plugin.getName(), "Deleted all NPCs."));
		Recharge.Instance.recharge(caller, RECHARGE_KEY);
	}
}
