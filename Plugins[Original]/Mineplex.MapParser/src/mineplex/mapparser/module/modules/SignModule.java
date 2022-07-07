package mineplex.mapparser.module.modules;

import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.module.Module;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 */
public class SignModule extends Module
{
	public SignModule(MapParser plugin)
	{
		super("Sign", plugin);
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void signChangeLog(SignChangeEvent event)
	{
		if (GetData(event.getPlayer().getWorld().getName()).HasAccess(event.getPlayer()))
		{
			ArrayList<String> text = new ArrayList<>();

			text.add("Date: " + UtilTime.now());
			text.add("Player: " + event.getPlayer().getName());
			text.add("Location: " + UtilWorld.locToStrClean(event.getBlock().getLocation()));
			for (int i = 0; i < event.getLines().length; i++)
			{
				text.add("Line " + i + ": " + event.getLines()[i]);
			}
			writeSignLog(text, event.getPlayer().getWorld());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void signCommand(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().toLowerCase().contains("set"))
		{
			ArrayList<String> text = new ArrayList<>();

			text.add("Date: " + UtilTime.now());
			text.add("Player: " + event.getPlayer().getName());
			text.add("Location: " + UtilWorld.locToStrClean(event.getPlayer().getLocation()));
			text.add("Message: " + event.getMessage());

			writeSignCommandLog(text, event.getPlayer().getWorld());
		}
	}

	public void writeSignCommandLog(ArrayList<String> text, World world)
	{
		try
		{
			File file = new File(world.getName() + "/" + "command_sign_log.txt");

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("\n\n");
			for (String line : text)
				bw.write("\n" + line);

			bw.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void writeSignLog(ArrayList<String> text, World world)
	{
		try
		{
			File file = new File(world.getName() + "/" + "sign_log.txt");

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("\n\n");
			for (String line : text)
				bw.write("\n" + line);

			bw.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
