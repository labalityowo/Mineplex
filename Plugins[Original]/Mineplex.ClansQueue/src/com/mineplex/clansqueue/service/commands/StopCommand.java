package com.mineplex.clansqueue.service.commands;

import com.mineplex.clansqueue.service.QueueService;

public class StopCommand extends ConsoleCommand
{
	private final QueueService _service;
	
	public StopCommand(QueueService service)
	{
		super("stop", "Stops the Queue Service");
		
		_service = service;
	}

	@Override
	protected void use(String[] arguments)
	{
		_service.shutdown();
	}
}