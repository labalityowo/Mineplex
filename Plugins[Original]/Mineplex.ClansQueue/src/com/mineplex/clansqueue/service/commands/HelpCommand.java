package com.mineplex.clansqueue.service.commands;

import java.util.Map;

public class HelpCommand extends ConsoleCommand
{
	private final Map<String, ConsoleCommand> _commands;
	
	public HelpCommand(Map<String, ConsoleCommand> commands)
	{
		super("help", "Lists commands and their usage");
		
		_commands = commands;
	}

	@Override
	protected void use(String[] arguments)
	{
		if (arguments.length < 1)
		{
			addOutput("Commands:");
			_commands.values().forEach(command ->
			{
				addOutput(command.getCommand() + " : " + command.getUsageText());
			});
		}
		else
		{
			if (_commands.containsKey(arguments[0].toLowerCase()))
			{
				ConsoleCommand cmd = _commands.get(arguments[0].toLowerCase());
				addOutput(cmd.getCommand() + " : " + cmd.getUsageText());
			}
			else
			{
				addOutput("Command '" + arguments[0] + "' was not found. Run 'help' for a list of commands.");
			}
		}
		sendOutput();
	}
}