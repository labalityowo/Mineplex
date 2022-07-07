package mineplex.staffServer.salespackage.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.command.CommandBase;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class LifetimeEternalCommand extends CommandBase<SalesPackageManager>
{
	public LifetimeEternalCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "lifetimeeternal");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales rank " + args[0] + " ETERNAL"));
		
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales item " + args[0] + " 2 Item Mythical Chest"));
		
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales item " + args[0] + " 2 Item Illuminated Chest"));
		
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales item " + args[0] + " 1 Item Omega Chest"));
	}
}
