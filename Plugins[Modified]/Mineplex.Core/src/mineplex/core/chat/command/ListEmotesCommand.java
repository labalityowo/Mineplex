package mineplex.core.chat.command;

import org.bukkit.entity.Player;

import mineplex.core.chat.Chat;
import mineplex.core.chat.Chat.Perm;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;

public class ListEmotesCommand extends CommandBase<Chat>
{

	public ListEmotesCommand(Chat plugin)
	{
		super(plugin, Perm.CHAT_EXTRA_COMMAND, "emotes");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Emotes List:"));
		Chat.EMOTES.forEach((emote, how) -> caller.sendMessage(F.elem(how) + " -> " + F.elem(emote)));
	}

}
