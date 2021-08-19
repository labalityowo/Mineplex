package mineplex.core.party.command;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.PartyManager;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;

public class PartyToggleCommand extends CommandBase<PartyManager>
{
	private final PreferencesManager _preferencesManager = Managers.require(PreferencesManager.class);

	public PartyToggleCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "toggle", "t");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		_preferencesManager.get(caller).toggle(Preference.PARTY_DISPLAY_INVENTORY_UI);
		if (_preferencesManager.get(caller).isActive(Preference.PARTY_DISPLAY_INVENTORY_UI))
		{
			UtilPlayer.message(caller, F.main("Party", "The Party GUI is now " + C.cGreen + "enabled" + C.mBody + "!"));
		}
		else
		{
			UtilPlayer.message(caller, F.main("Party", "The Party GUI is now " + C.cRed + "disabled" + C.mBody + "!"));
		}
	}
}