package mineplex.core.mission.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionManager.Perm;

public class DebugMissionCommand extends CommandBase<MissionManager>
{

	public DebugMissionCommand(MissionManager plugin)
	{
		super(plugin, Perm.DEBUG_MISSION_COMMAND, "missiondebug");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Mission Debug: " + F.ed(Plugin.toggleDebug())));
	}
}
