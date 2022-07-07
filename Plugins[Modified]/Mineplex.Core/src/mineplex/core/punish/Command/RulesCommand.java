package mineplex.core.punish.Command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.punish.Punish;

public class RulesCommand extends CommandBase<Punish>
{
	private static final String RULES_MESSAGE = "The rules can be found here:" + C.cGreen + " www.mineplex.com/rules";
	
	public RulesCommand(Punish plugin)
	{
		super(plugin, Punish.Perm.RULES_COMMAND, "rules");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		caller.sendMessage(F.main("Rules", RULES_MESSAGE));
	}
}