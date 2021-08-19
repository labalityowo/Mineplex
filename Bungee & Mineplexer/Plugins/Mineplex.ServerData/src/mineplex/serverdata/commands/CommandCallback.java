package mineplex.serverdata.commands;

public interface CommandCallback<T extends ServerCommand>
{
	void run(T command);
}