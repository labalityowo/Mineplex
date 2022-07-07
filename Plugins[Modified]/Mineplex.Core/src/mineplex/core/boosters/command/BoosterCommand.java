package mineplex.core.boosters.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

import static mineplex.core.Managers.require;

/**
 * @author Shaun Bennett
 */
public class BoosterCommand extends MultiCommandBase<BoosterManager>
{
	CoreClientManager _coreClientManager;

	public BoosterCommand(BoosterManager plugin)
	{
		super(plugin, BoosterManager.Perm.BOOSTER_COMMAND, "amplifier");

		_coreClientManager = require(CoreClientManager.class);

		AddCommand(new AddCommand(plugin));
		AddCommand(new GuiCommand(plugin));
		AddCommand(new ReloadCommand(plugin));
		AddCommand(new ThankCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		if (_coreClientManager.Get(caller).hasPermission(BoosterManager.Perm.ADD_BOOSTER_COMMAND))
		{
			UtilPlayer.message(caller, F.help("amplifier add <group>", "Add an amplifier to that group", ChatColor.DARK_RED));
		}

		if (_coreClientManager.Get(caller).hasPermission(BoosterManager.Perm.BOOSTER_GUI_COMMAND))
		{
			UtilPlayer.message(caller, F.help("amplifier gui", "Open Amplifier GUI", ChatColor.DARK_RED));
		}

		if (_coreClientManager.Get(caller).hasPermission(BoosterManager.Perm.THANK_COMMAND))
		{
			UtilPlayer.message(caller, F.help("amplifier thank <group>", "Thank an Amplifier for a specific Booster Group", ChatColor.WHITE));
		}
	}
}