package mineplex.hub.gimmicks.staffbuild;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.hub.gimmicks.staffbuild.StaffBuild.Perm;

public class ClearBuildCommand extends CommandBase<StaffBuild>
{

	ClearBuildCommand(StaffBuild plugin)
	{
		super(plugin, Perm.CLEAR_BUILD, "clearbuild");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.getBuildHistory().keySet().forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR));
		Plugin.getBuildHistory().clear();
		caller.sendMessage(F.main(Plugin.getName(), "Cleared the staff build area."));
	}
}
