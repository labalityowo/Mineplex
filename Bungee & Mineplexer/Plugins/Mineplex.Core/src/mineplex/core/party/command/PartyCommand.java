package mineplex.core.party.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.command.MultiCommandBase;
import mineplex.core.party.PartyManager;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;

/**
 * Command handler for party commands
 */
public class PartyCommand extends MultiCommandBase<PartyManager>
{
	private final PreferencesManager _preferencesManager = Managers.require(PreferencesManager.class);

	public PartyCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "party", "z");

		AddCommand(new PartyGuiCommand(plugin));
		AddCommand(new PartyCLICommand(plugin));
		AddCommand(new PartyToggleCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		if (args.length > 0)
		{
			if (!args[0].equalsIgnoreCase("cli") && !args[0].equalsIgnoreCase("gui"))
			{
				String[] newArgs = new String[args.length + 1];
				System.arraycopy(args, 0, newArgs, 1, args.length);
				newArgs[0] = _preferencesManager.get(caller).isActive(Preference.PARTY_DISPLAY_INVENTORY_UI) ? "gui" : "cli";
				args = newArgs;
			}
		}
		else
		{
			String[] newArgs = new String[1];
			newArgs[0] = _preferencesManager.get(caller).isActive(Preference.PARTY_DISPLAY_INVENTORY_UI) ? "gui" : "cli";
			args = newArgs;
		}
		Execute(caller, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (sender instanceof Player)
		{
			if (args.length > 0)
			{
				if (!args[0].equalsIgnoreCase("cli") && !args[0].equalsIgnoreCase("gui"))
				{
					String[] newArgs = new String[args.length + 1];
					System.arraycopy(args, 0, newArgs, 1, args.length);
					newArgs[0] = _preferencesManager.get((Player) sender).isActive(Preference.PARTY_DISPLAY_INVENTORY_UI) ? "gui" : "cli";
					args = newArgs;
				}
			}
			else
			{
				String[] newArgs = new String[1];
				newArgs[0] = _preferencesManager.get((Player) sender).isActive(Preference.PARTY_DISPLAY_INVENTORY_UI) ? "gui" : "cli";
				args = newArgs;
			}
		}
		return super.onTabComplete(sender, commandLabel, args);
	}
}