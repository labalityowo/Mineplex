package com.mineplex.clansqueue.service.commands;

import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.mineplex.clansqueue.service.QueueService;

public class CommandSystem extends Thread
{
	private final QueueService _service;
	private final Map<String, ConsoleCommand> _commands;
	
	public CommandSystem(QueueService service, Map<String, ConsoleCommand> commands)
	{
		super("Command System");
		_service = service;
		_commands = commands;
		
		_service.registerCommand(new HelpCommand(_commands));
		_service.registerCommand(new StopCommand(_service));
		_service.registerCommand(new DeleteQueueCommand(_service));
		_service.registerCommand(new ListQueuesCommand(_service));
		_service.registerCommand(new PauseQueueCommand(_service));
		_service.registerCommand(new UnpauseQueueCommand(_service));
	}
	
	private boolean matches(String key, String input)
	{
		if (key.equalsIgnoreCase(input))
		{
			return true;
		}
		if (input.toLowerCase().startsWith(key + " "))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		try (Scanner scanner = new Scanner(System.in))
		{
			while (_service.isRunning())
			{
				String input = scanner.nextLine();
				if (input.isEmpty())
				{
					continue;
				}
				Optional<ConsoleCommand> opt = _commands.entrySet().stream().filter(entry -> matches(entry.getKey(), input)).map(Map.Entry::getValue).findAny();
				if (opt.isPresent())
				{
					opt.get().call(input);
				}
				else
				{
					System.out.println("Command '" + input.split(" ")[0] + "' was not found. Run 'help' for a list of commands.");
				}
			}
		}
	}
}