package mineplex.gemhunters.economy.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.recharge.Recharge;
import mineplex.gemhunters.economy.CashOutModule;

public class ResetCooldownCommand extends CommandBase<CashOutModule>
{
	public ResetCooldownCommand(CashOutModule plugin)
	{
		super(plugin, CashOutModule.Perm.RESET_COOLDOWN_COMMAND, "resetcashout");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Recharge.Instance.useForce(caller, "Cash Out", 0);
	}
}