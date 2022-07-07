package mineplex.core.mission.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionManager.Perm;

public class ViewMissionsCommand extends CommandBase<MissionManager>
{

	public ViewMissionsCommand(MissionManager plugin)
	{
		super(plugin, Perm.VIEW_MISSION_COMMAND, "missions", "mission");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.getShop().attemptShopOpen(caller);
	}
}
