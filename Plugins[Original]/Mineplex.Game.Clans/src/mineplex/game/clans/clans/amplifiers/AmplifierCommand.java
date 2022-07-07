package mineplex.game.clans.clans.amplifiers;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;

/**
 * Main amplifier command
 */
public class AmplifierCommand extends CommandBase<AmplifierManager>
{
	public AmplifierCommand(AmplifierManager plugin)
	{
		super(plugin, AmplifierManager.Perm.AMPLIFIER_COMMAND, "amplifier", "runeamplifier");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		new AmplifierGUI(caller, Plugin);
	}
}