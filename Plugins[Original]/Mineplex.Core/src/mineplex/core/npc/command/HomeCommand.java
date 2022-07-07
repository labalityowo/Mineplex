package mineplex.core.npc.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.npc.NpcManager;

public class HomeCommand extends CommandBase<NpcManager>
{
	public HomeCommand(NpcManager plugin)
	{
		super(plugin, NpcManager.Perm.NPC_HOME_COMMAND, "home");
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
			Plugin.teleportNpcsHome();

			UtilPlayer.message(caller, F.main(Plugin.getName(), "Npcs teleported to home locations."));
		}
	}
}