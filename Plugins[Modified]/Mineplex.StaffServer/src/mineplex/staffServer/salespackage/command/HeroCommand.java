package mineplex.staffServer.salespackage.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.command.CommandBase;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class HeroCommand extends CommandBase<SalesPackageManager>
{
	public HeroCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "hero");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales rank " + args[0] + " HERO false"));
		
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales coin " + args[0] + " 15000"));
		
		resetCommandCharge(caller);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCommandPreprocessEvent(caller, "/sales booster " + args[0] + " 90"));
	}
}