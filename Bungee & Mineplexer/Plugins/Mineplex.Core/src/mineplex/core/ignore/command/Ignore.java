package mineplex.core.ignore.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.Callback;
import mineplex.core.ignore.IgnoreManager;

public class Ignore extends CommandBase<IgnoreManager>
{
    public Ignore(IgnoreManager plugin)
    {
        super(plugin, IgnoreManager.Perm.IGNORE_COMMAND, "ignore");
    }

    @Override
    public void Execute(final Player caller, final String[] args)
    {
        if (args == null || args.length < 1)
        {
            Plugin.showIgnores(caller);
        }
        else
        {
            _commandCenter.GetClientManager().checkPlayerName(caller, args[0], new Callback<String>()
            {
                public void run(String result)
                {
                    if (result != null)
                    {
                        Plugin.addIgnore(caller, result);
                    }
                }
            });
        }
    }
}