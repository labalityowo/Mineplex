package mineplex.mapparser.command;

import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;

import mineplex.mapparser.MapParser;
import org.apache.commons.io.FileUtils;

/**
 * Created by Shaun on 8/16/2014.
 */
public class CopySchematicsCommand extends BaseCommand
{
	public CopySchematicsCommand(MapParser plugin)
	{
		super(plugin, "copyschematics");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		try
		{
			FileUtils.copyDirectory(new File(".." + File.separator + "Build-1" + File.separator + "plugins" + File.separator + "WorldEdit" + File.separator + "schematics"),
					new File("plugins" + File.separator + "WorldEdit" + File.separator + "schematics"));

			message(player, "Schematics Copied.");
		}
		catch (IOException e)
		{
			e.printStackTrace();

			message(player, "Schematics Copy Failed! Contact Jonalon.");
		}

		return true;
	}
}
