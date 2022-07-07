package mineplex.core.incognito.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.preferences.Preference;

public class IncognitoToggleCommand extends CommandBase<IncognitoManager>
{
	public IncognitoToggleCommand(IncognitoManager plugin)
	{
		super(plugin, IncognitoManager.Perm.USE_INCOGNITO, "incognito", "vanish", "v");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (Plugin.getPreferences().get(caller).isActive(Preference.INVISIBILITY) && !Plugin.Get(caller).Status)
		{
			UtilPlayer.message(caller, F.main("Incognito", "You are not allowed to toggle incognito on while Hub Invisibility is enabled."));
			return;
		}

		if (Plugin.toggle(caller))
		{
			UtilPlayer.message(caller, F.main("Incognito", "You are now incognito. Your status will only change when you run " + F.elem("/" + _aliasUsed) + " again."));
		}
		else
		{
			UtilPlayer.message(caller, F.main("Incognito", "You are no longer incognito. Your status will only change when you run " + F.elem("/" + _aliasUsed) + " again."));
		}
	}
}