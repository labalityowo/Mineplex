package mineplex.game.nano.commands.spectator;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.NanoManager.Perm;

public class SpectatorCommand extends CommandBase<NanoManager>
{

	public SpectatorCommand(NanoManager plugin)
	{
		super(plugin, Perm.SPECTATOR_COMMAND, "spec", "spectate");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (Plugin.getIncognitoManager().Get(caller).Status)
		{
			caller.sendMessage(F.main(Plugin.getName(), "You cannot toggle spectator mode while vanished."));
			return;
		}

		boolean spectator = !Plugin.isSpectator(caller);

		Plugin.setSpectator(caller, spectator);

		if (spectator)
		{
			caller.sendMessage(F.main(Plugin.getName(), "You will be a spectator in the next game."));
		}
		else
		{
			caller.sendMessage(F.main(Plugin.getName(), "You will participate in the next game."));
		}
	}
}
