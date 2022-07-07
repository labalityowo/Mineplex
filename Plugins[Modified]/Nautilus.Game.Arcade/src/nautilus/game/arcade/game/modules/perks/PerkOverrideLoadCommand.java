package nautilus.game.arcade.game.modules.perks;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.SuperSmash.Perm;

public class PerkOverrideLoadCommand extends CommandBase<ArcadeManager>
{

	private final PerkSpreadsheetModule _module;

	public PerkOverrideLoadCommand(ArcadeManager manager, PerkSpreadsheetModule module)
	{
		super(manager, Perm.DEBUG_PERK_COMMANDS, "perkload");

		_module = module;
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		_module.setup();
		caller.sendMessage(F.main("Game", "Overridden Values:"));

		PerkSpreadsheetModule.getValueOverrideMap().forEach((key, value) -> caller.sendMessage(F.main("Game", F.name(key) + " -> " + F.elem(value) + ".")));
	}
}
