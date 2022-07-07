package nautilus.game.pvp.modules.serverreset;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;

public class ServerReset extends MiniPlugin
{
	private ServerResetRepository _repository;
	
	public ServerReset(JavaPlugin plugin, String webAddress)
	{
		super("Server Reset", plugin);
		
		_repository = new ServerResetRepository(webAddress);
	}
	
	public void AddCommands()
	{
		addCommand(new ServerResetCommand(this));
	}

	public ServerResetRepository GetRepository()
	{
		return _repository;
	}
}
