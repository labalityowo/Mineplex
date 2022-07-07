package mineplex.gemhunters.quest.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.gemhunters.quest.QuestModule;
import mineplex.gemhunters.quest.QuestPlayerData;

public class ResetQuestsCommand extends CommandBase<QuestModule>
{
	public ResetQuestsCommand(QuestModule plugin)
	{
		super(plugin, QuestModule.Perm.RESET_QUESTS_COMMAND, "resetquest");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Player target = caller;
		
		if (args.length > 0)
		{
			Player arg = UtilPlayer.searchOnline(target, args[0], true);
			target = arg == null ? target : arg;
		}
		
		if (caller.equals(target))
		{
			caller.sendMessage(F.main(Plugin.getName(), "Reset your quests."));
		}
		else
		{
			caller.sendMessage(F.main(Plugin.getName(), "You reset " + F.elem(target.getName() + "'s") + " quests."));
			target.sendMessage(F.main(Plugin.getName(), F.elem(caller.getName()) + " reset your quests."));
		}
		
		QuestPlayerData playerData = Plugin.Get(target);
		
		playerData.clear();
		Plugin.updateQuests(target);
	}
}