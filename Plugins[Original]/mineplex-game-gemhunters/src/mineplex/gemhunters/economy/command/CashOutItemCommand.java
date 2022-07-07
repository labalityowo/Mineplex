package mineplex.gemhunters.economy.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.gemhunters.economy.CashOutModule;

public class CashOutItemCommand extends CommandBase<CashOutModule>
{
	public CashOutItemCommand(CashOutModule plugin)
	{
		super(plugin, CashOutModule.Perm.CASH_OUT_ITEM_COMMAND, "cashout", "ct", "cashitem", "cashoutitem");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (caller.getInventory().contains(CashOutModule.CASH_OUT_ITEM))
		{
			return;
		}
		
		caller.sendMessage(F.main(Plugin.getName(), "Giving you a new cash out item."));
		caller.getInventory().addItem(CashOutModule.CASH_OUT_ITEM);
	}
}