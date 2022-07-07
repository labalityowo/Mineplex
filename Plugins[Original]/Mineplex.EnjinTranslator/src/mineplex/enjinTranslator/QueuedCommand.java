package mineplex.enjinTranslator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class QueuedCommand
{
	public CommandSender Sender;
	public Command Command;
	public String Label;
	public String[] Args;
	
	public QueuedCommand(CommandSender sender, Command command, String label, String...args)
	{
		Sender = sender;
		Command = command;
		Label = label;
		Args = args;
	}
}
