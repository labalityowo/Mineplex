package mineplex.mapparser.command;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 */
public class RefreshWorldEditCommand extends BaseCommand
{

	public RefreshWorldEditCommand(MapParser plugin)
	{
		super(plugin, "refreshworldedit", "refreshwe", "wefresh");
		setUsage("/refreshwe");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		Bukkit.broadcastMessage(F.name(player.getName()) + " is reloading World Edit");
		Plugin plugin = getPlugin().getServer().getPluginManager().getPlugin("WorldEdit");
		plugin.onDisable();
		plugin.onEnable();
		Bukkit.broadcastMessage(F.name(player.getName()) + " has reloaded World Edit");
		return true;
	}
}
