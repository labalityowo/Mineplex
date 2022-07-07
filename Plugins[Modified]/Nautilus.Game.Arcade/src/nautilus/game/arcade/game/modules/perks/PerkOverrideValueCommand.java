package nautilus.game.arcade.game.modules.perks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.SuperSmash.Perm;

public class PerkOverrideValueCommand extends CommandBase<ArcadeManager>
{

	private final PerkSpreadsheetModule _module;

	public PerkOverrideValueCommand(ArcadeManager manager, PerkSpreadsheetModule module)
	{
		super(manager, Perm.DEBUG_PERK_COMMANDS, "perkoverride");

		_module = module;
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			sendUsage(caller);
			return;
		}

		StringBuilder builder = new StringBuilder();

		// Ignore the last argument
		for (int i = 0; i < args.length - 1; i++)
		{
			builder.append(args[i]).append(" ");
		}

		String key = builder.toString().trim();
		String value = args[args.length - 1];

		if (!_module.getDataMap().containsKey(key))
		{
			caller.sendMessage(F.main("Game", "That is not a valid key."));
			return;
		}
		else if (value.equalsIgnoreCase("clear"))
		{
			PerkSpreadsheetModule.getValueOverrideMap().remove(key);
			caller.sendMessage(F.main("Game", "Reset the perk variable " + F.name(key) + " to it's default value."));
			return;
		}

		PerkSpreadsheetModule.getValueOverrideMap().put(key, value);
		caller.sendMessage(F.main("Game", "Overrode the perk variable " + F.name(key) + " with value " + F.elem(value) + "."));
	}

	private void sendUsage(Player caller)
	{
		caller.sendMessage(F.main("Game", "/" + _aliasUsed + " <key> <value>"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		return new ArrayList<>(_module.getDataMap().keySet());
	}
}
