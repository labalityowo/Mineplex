package nautilus.game.arcade.command;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameServerConfig;
import nautilus.game.arcade.managers.IdleManager;
import org.bukkit.entity.Player;

public class ToggleAfkKickCommand extends CommandBase<ArcadeManager>
{
    public ToggleAfkKickCommand (ArcadeManager plugin)
    {
        super(plugin, IdleManager.Perm.TOGGLE_AFK_KICK_COMMAND, "toggleafkkick", "afkkick");
    }

    public void toggleAfkKick()
    {
        GameServerConfig config = Plugin.GetServerConfig();

        config.PlayerKickIdle = !config.PlayerKickIdle;
    }

    @Override
    public void Execute(Player caller, String[] args)
    {
        toggleAfkKick();

        String message = C.cWhiteB + "AFK kick is now ";

        if (Plugin.IsPlayerKickIdle())
        {
            message += C.cGreenB + "enabled";
        }
        else
        {
            message += C.cRedB + "disabled";
        }

        message += C.cWhiteB + ".";

        String finalMessage = message;
        UtilServer.getPlayersCollection().forEach(p -> p.sendMessage(finalMessage));
    }
}
