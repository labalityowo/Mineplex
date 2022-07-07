package com.mineplex.clansqueue.service.commands;

public abstract class ConsoleCommand
{
	private final String _command;
	private final String _usageText;
	private StringBuilder _outputBuilder;
	
	public ConsoleCommand(String command, String usageText)
	{
		_command = command;
		_usageText = usageText;
	}
	
	public String getCommand()
	{
		return _command;
	}
	
	public String getUsageText()
	{
		return _usageText;
	}
	
	protected final void addOutput(String text)
	{
		if (_outputBuilder == null)
		{
			_outputBuilder = new StringBuilder();
		}
		else
		{
			_outputBuilder.append("\n");
		}
		_outputBuilder.append(text);
	}
	
	protected final void sendOutput()
	{
		System.out.println(_outputBuilder.toString());
		_outputBuilder = null;
	}
	
	public final void call(String input)
	{
		String parsing = input.trim();
		if (parsing.length() > getCommand().length() + 2)
		{
			String[] args = parsing.substring(getCommand().length() + 1).split(" ");
			use(args);
		}
		else
		{
			use(new String[] {});
		}
	}
	
	protected abstract void use(String[] arguments);
}