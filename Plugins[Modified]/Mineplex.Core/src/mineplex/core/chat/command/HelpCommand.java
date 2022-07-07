package mineplex.core.chat.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.account.permissions.Permission;
import mineplex.core.chat.Chat;
import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilText;

public class HelpCommand extends CommandBase<Chat>
{

	public HelpCommand(Chat plugin)
	{
		super(plugin, Chat.Perm.HELP_COMMAND, "help", "?", "pleasehelpmeiamgoingtoexplode");
	}

	private void sendMain(Player caller, String message)
	{
		caller.sendMessage(F.main("Help", message));
	}

	private void sendSingle(Player caller, String message)
	{
		caller.sendMessage(F.main("", message));
	}

	private JsonMessage getSingleStart(String before)
	{
		return new JsonMessage(C.cBlue + "> ")
				.extra(before)
					.color(C.cGray);
	}

	private void sendUrl(Player caller, String before, String clickText, ChatColor clickTextColor, String url, String hoverText)
	{
		getSingleStart(before)
				.color(UtilColor.chatColorToJsonColor(ChatColor.GRAY))
				.extra(clickText)
					.color(UtilColor.chatColorToJsonColor(clickTextColor))
					.click(ClickEvent.OPEN_URL, url)
					.hover(HoverEvent.SHOW_TEXT, hoverText)
				.sendToPlayer(caller);
	}

	private void sendFaq(Player caller)
	{
		sendUrl(caller,
				"Check out ",
				 "our FAQ for commonly asked questions", ChatColor.YELLOW,
				"http://www.mineplex.com/faq",
				C.cYellow + "Click to visit our FAQ page!");
	}

	private void sendRules(Player caller)
	{
		sendUrl(caller,
				"Read ",
				"our rules to avoid being punished!", ChatColor.YELLOW,
				"http://www.mineplex.com/rules",
				C.cYellow + "Click to visit our rules page!");
	}

	private void sendTrainee(Player caller)
	{
		sendUrl(caller,
				"Want to apply for Trainee? Visit ",
				"apply.mineplex.com" + C.cGray + "!", ChatColor.YELLOW,
				"http://apply.mineplex.com",
				C.cDAqua + "Click to visit our forums to learn about Trainee!");
	}

	private void sendSupport(Player caller)
	{
		sendUrl(caller,
				"Question about a purchase? Contact support at ",
				"mineplex.com/support" + C.cGray + "!", ChatColor.YELLOW,
				"http://www.mineplex.com/support",
				C.cYellow + "Click to visit our support page!");
	}

	private void sendTwitter(Player caller)
	{
		sendUrl(caller,
				"Find us on twitter at ",
				"@Mineplex", ChatColor.AQUA,
				"https://twitter.com/Mineplex",
				C.cAqua + "Click to visit our twitter!");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		sendMain(caller, "Hi there! Need some help?");
		sendFaq(caller);
		sendRules(caller);
		sendTrainee(caller);
		sendSupport(caller);
		sendTwitter(caller);
	}
}
