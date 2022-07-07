package mineplex.game.clans.items.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.runes.RuneManager;
import mineplex.game.clans.items.runes.RuneManager.RuneAttribute;

/**
 * Command to give yourself a rune
 */
public class RuneCommand extends CommandBase<GearManager>
{
	private RuneManager _rune;
	
	public RuneCommand(GearManager plugin)
	{
		super(plugin, GearManager.Perm.RUNE_COMMAND, "rune", "giverune", "getrune");
	}
	
	@Override
	public void Execute(Player caller, String[] args)
	{
		if (_rune == null)
		{
			_rune = Plugin.getRuneManager();
		}
		
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.main("Rune", "Usage: /" + _aliasUsed + " <Rune Type>"));
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Available types:"));
			for (RuneAttribute rune : RuneAttribute.values())
			{
				UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + rune.toString());
			}
			return;
		}
		RuneAttribute rune = RuneAttribute.getFromString(args[0]);
		if (rune == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Invalid rune type! Available types:"));
			for (RuneAttribute type : RuneAttribute.values())
			{
				UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + type.toString());
			}
			return;
		}
		UtilInv.insert(caller, _rune.getRune(rune));
		UtilPlayer.message(caller, F.main("Rune", "You have been given a(n) " + F.elem(rune.getDisplay()) + " rune!"));
	}	
}