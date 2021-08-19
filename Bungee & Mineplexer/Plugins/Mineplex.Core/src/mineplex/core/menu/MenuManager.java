package mineplex.core.menu;

import mineplex.core.MiniPlugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 */
@Deprecated
public class MenuManager extends MiniPlugin
{

	public MenuManager(JavaPlugin plugin)
	{
		super("Menu Manager", plugin);
		getPluginManager().registerEvents(new MenuListener(), plugin);
	}

}
