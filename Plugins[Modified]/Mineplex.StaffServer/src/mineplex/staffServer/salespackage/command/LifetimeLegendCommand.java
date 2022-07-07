package mineplex.staffServer.salespackage.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.command.CommandBase;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class LifetimeLegendCommand extends CommandBase<SalesPackageManager>
{
	public LifetimeLegendCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "lifetimelegend");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales rank " + args[0] + " LEGEND"));
		
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales item " + args[0] + " 3 Item Mythical Chest"));
	}
}
