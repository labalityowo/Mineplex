package mineplex.staffServer.salespackage.command;

import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class Sales extends MultiCommandBase<SalesPackageManager>
{
	public Sales(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "sales");
		
		AddCommand(new RankCommand(plugin));
		AddCommand(new CoinCommand(plugin));
		AddCommand(new ItemCommand(plugin));
		AddCommand(new GemHunterCommand(plugin));
		AddCommand(new UltraCommand(plugin));
		AddCommand(new HeroCommand(plugin));
		AddCommand(new LifetimeUltraCommand(plugin));
		AddCommand(new LifetimeHeroCommand(plugin));
		AddCommand(new LifetimeLegendCommand(plugin));
		AddCommand(new LifetimeTitanCommand(plugin));
		AddCommand(new LifetimeEternalCommand(plugin));
		AddCommand(new KitsCommand(plugin));
		AddCommand(new PowerPlayCommand(plugin));
		AddCommand(new PetCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args) {}
}