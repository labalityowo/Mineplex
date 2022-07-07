package mineplex.core.preferences.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.preferences.PreferencesManager;

public class PreferencesCommand extends CommandBase<PreferencesManager>
{
	public PreferencesCommand(PreferencesManager plugin)
	{
		super(plugin, PreferencesManager.Perm.PREFERENCES_COMMAND, "pref", "prefs");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.openMenu(caller);
	}
}