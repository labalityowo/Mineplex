package com.mineplex.clansqueue.service.commands;

import com.mineplex.clansqueue.service.QueueService;
import com.mineplex.clansqueue.service.queue.ClansServer;

public class UnpauseQueueCommand extends ConsoleCommand
{
	private final QueueService _service;
	
	public UnpauseQueueCommand(QueueService service)
	{
		super("unpause", "Resumes an existing queue");
		
		_service = service;
	}

	@Override
	protected void use(String[] arguments)
	{
		if (arguments.length < 1)
		{
			addOutput("Usage: unpause <Server>");
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
		
		_service.getQueueManager().handleQueuePause(server.getName(), false);
		addOutput("Queue unpaused.");
		sendOutput();
	}
}