package mineplex.core.updater.command;

import java.util.Properties;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.updater.FileUpdater;

public class BuildVersionCommand extends CommandBase<FileUpdater>
{
	public BuildVersionCommand(FileUpdater plugin)
	{
		super(plugin, FileUpdater.Perm.BVERSION_COMMAND, "bversion");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Properties buildProperties = Plugin.getBuildProperties();

		String git = buildProperties.getProperty("build.git", "Unknown");
		String date = buildProperties.getProperty("build.date", "Unknown");
		String user = buildProperties.getProperty("build.user", "Unknown");

		caller.sendMessage(C.cRedB + "Build Version;");
		caller.sendMessage("  " + C.cGold + "Date " + C.cWhite + date);
		caller.sendMessage("  " + C.cGold + "User " + C.cWhite + user);
		caller.sendMessage("  " + C.cGold + "Git " + C.cWhite + git);
	}
}