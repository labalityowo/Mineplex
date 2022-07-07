package mineplex.core.report;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.report.command.ReportCloseCommand;
import mineplex.core.report.command.ReportCommand;
import mineplex.core.report.command.ReportHandleCommand;
import mineplex.core.report.command.ReportInfoCommand;
import mineplex.core.report.command.ReportHistoryCommand;
import mineplex.core.report.command.ReportMetricsCommand;

/**
 * Main class for this module, handles initialization and disabling of the module.
 */
public class ReportPlugin extends MiniPlugin 
{
	private final ReportManager _manager;

	public ReportPlugin(JavaPlugin plugin, ReportManager manager)
	{
		super("Report", plugin);
		_manager = manager;
	}

	public ReportManager getManager()
	{
		return _manager;
	}

	@Override
	public void addCommands()
	{
		addCommand(new ReportCommand(this));
		addCommand(new ReportHandleCommand(this));
		addCommand(new ReportCloseCommand(this));
		addCommand(new ReportHistoryCommand(this));
		addCommand(new ReportInfoCommand(this));
		addCommand(new ReportMetricsCommand(this));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		_manager.onPlayerJoin(e.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		_manager.onPlayerQuit(e.getPlayer());
	}
}