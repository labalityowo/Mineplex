package mineplex.mapparser.module;

import mineplex.core.common.util.C;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 *
 */
public abstract class Module implements Listener
{

	private MapParser _plugin;
	private String _name;

	public Module(String name, MapParser plugin) {
		_name = name;
		_plugin = plugin;
		register();
		plugin.getModules().put(this.getClass(), this);
	}

	public void register()
	{
		_plugin.getServer().getPluginManager().registerEvents(this, _plugin);
	}

	public MapParser getPlugin()
	{
		return _plugin;
	}

	public MapData GetData(String world)
	{
		return getPlugin().getData(world);
	}

	public void displayHelp(Player player)
	{
		MapData data = GetData(player.getWorld().getName());
		player.sendMessage(C.cGray + "Currently Live: " + (data._currentlyLive ? C.cGreen + "True" : C.cRed + "False"));
		for(String s : getPlugin().getAdditionalText())
		{
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
		}
	}
}
