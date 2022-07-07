package mineplex.mapparser.command;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 */
public class AddSplashTextCommand extends OpCommand
{

	public AddSplashTextCommand(MapParser plugin)
	{
		super(plugin, "addtext");
		setUsage("/addText <text>");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if(args.length == 0)
		{
			return false;
		}

		if(args[0].equalsIgnoreCase("clear"))
		{
			getPlugin().getAdditionalText().clear();
			player.sendMessage(F.main(getPlugin().getName(), "Cleared all splash text!"));
			return true;
		}

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < args.length; i++)
		{
			builder.append(args[i]);
			if ((i + 1) != args.length)
			{
				builder.append(" ");
			}
		}

		getPlugin().addAdditionalText(builder.toString());
		player.sendMessage(F.main(getPlugin().getName(), "Added splash text: "));
		player.sendMessage(F.main("", ChatColor.translateAlternateColorCodes('&', builder.toString())));

		return true;
	}
}
