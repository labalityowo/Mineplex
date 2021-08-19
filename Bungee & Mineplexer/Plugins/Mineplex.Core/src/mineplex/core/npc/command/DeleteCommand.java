package mineplex.core.npc.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.npc.NpcManager;

public class DeleteCommand extends CommandBase<NpcManager>
{
	public DeleteCommand(NpcManager plugin)
	{
		super(plugin, NpcManager.Perm.DELETE_NPC_COMMAND, "del");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length > 0)
		{
			Plugin.help(caller);
		}
		else
		{
			Plugin.prepDeleteNpc(caller);

			UtilPlayer.message(caller, F.main(Plugin.getName(), "Now right click npc."));
		}
	}
}