package com.mineplex.clansqueue.service.commands;

import java.util.stream.Collectors;

import com.mineplex.clansqueue.service.QueueService;
import com.mineplex.clansqueue.service.queue.ClansServer;

public class ListQueuesCommand extends ConsoleCommand
{
	private final QueueService _service;
	
	public ListQueuesCommand(QueueService service)
	{
		super("list", "Lists existing servers");
		
		_service = service;
	}

	@Override
	protected void use(String[] arguments)
	{
		StringBuilder servers = new StringBuilder("Servers: [");
		servers.append(_service.getQueueManager().getLoadedServers().stream().map(ClansServer::getName).collect(Collectors.joining(", ")));
		servers.append(']');
		
		addOutput(servers.toString());
		sendOutput();
	}
}