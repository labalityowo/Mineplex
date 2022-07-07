package mineplex.mapparser.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.module.modules.CommandModule;

public class CommandListCommand extends BaseCommand
{
	private final static String MESSAGE_PREFIX = C.cGray + "• ";
	private CommandModule _commandModule;

	public CommandListCommand(MapParser plugin, CommandModule commandModule)
	{
		super(plugin, "commands");

		_commandModule = commandModule;
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		player.sendMessage(F.main(getPlugin().getName(), "Commands list:"));
		player.sendMessage("");
		player.sendMessage(C.cPurple + C.Italics + "Some of these commands may require OP/Map Admin");
		player.sendMessage("");

		List<String> commandsWithoutInformation = new ArrayList<>();
		List<String> commandsWithInformation = new ArrayList<>();

		for (Map.Entry<String, BaseCommand> entry : _commandModule.getCommands().entrySet())
		{
			BaseCommand command = entry.getValue();

			if (command.getUsage() != null || command.getDescription() != null)
			{
				String commandText = command.getUsage() == null ? entry.getKey() : command.getUsage();

				String message = MESSAGE_PREFIX + C.cYellow + commandText;

				if (command.getDescription() != null)
				{
					message += C.cGray + " - " + C.cGold + C.Italics + command.getDescription();
				}

				commandsWithInformation.add(message);
			}
			else
			{
				commandsWithoutInformation.add("/" + entry.getKey());
			}
		}

		if (commandsWithoutInformation.size() > 0)
		{
			player.sendMessage(MESSAGE_PREFIX + commandsWithoutInformation.stream().map(c -> C.cAqua + c).collect(Collectors.joining(C.cGray + ", ")));
		}

		if (commandsWithInformation.size() > 0)
		{
			player.sendMessage(commandsWithInformation.toArray(new String[0]));
		}

		return true;
	}
}
