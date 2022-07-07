package mineplex.mapparser.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.MapParser;

/**
 * Created by Shaun on 8/15/2014.
 */
public abstract class BaseCommand
{
	private MapParser _plugin;
	private List<String> _aliases;
	private String _description = null;
	private String _usage = null;

	public BaseCommand(MapParser plugin, String... aliases)
	{
		_plugin = plugin;
		_aliases = Arrays.asList(aliases);
	}

	public boolean canRun(Player player)
	{
		return true;
	}

	public abstract boolean execute(Player player, String alias, String[] args);

	public String getDescription()
	{
		return _description;
	}

	protected void setDescription(String description)
	{
		_description = description;
	}

	public String getUsage()
	{
		return _usage;
	}

	protected void setUsage(String usage)
	{
		_usage = usage;
	}

	public List<String> getAliases()
	{
		return _aliases;
	}

	protected MapParser getPlugin()
	{
		return _plugin;
	}

	protected void message(Player player, String message)
	{
		UtilPlayerBase.message(player, F.main("Parser", message));
	}
}
