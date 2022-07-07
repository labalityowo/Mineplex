package com.mineplex.clansqueue.service.commands;

import com.mineplex.clansqueue.service.QueueService;
import com.mineplex.clansqueue.service.queue.ClansServer;

public class PauseQueueCommand extends ConsoleCommand
{
	private final QueueService _service;
	
	public PauseQueueCommand(QueueService service)
	{
		super("pause", "Pauses an existing queue");
		
		_service = service;
	}

	@Override
	protected void use(String[] arguments)
	{
		if (arguments.length < 1)
		{
			addOutput("Usage: pause <Server>");
			sendOutput();
			return;
		}
		ClansServer server = _service.getQueueManager().getLoadedServer(arguments[0]);
		if (server == null)
		{
			addOutput("Server '" + arguments[0] + "' was not found. Run 'list' for a list of servers.");
			sendOutput();
			return;
		}
		
		_service.getQueueManager().handleQueuePause(server.getName(), true);
		addOutput("Queue paused.");
		sendOutput();
	}
}