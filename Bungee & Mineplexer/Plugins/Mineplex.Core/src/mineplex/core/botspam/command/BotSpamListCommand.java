package mineplex.core.botspam.command;

import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import mineplex.core.botspam.BotSpamManager;
import mineplex.core.botspam.SpamText;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class BotSpamListCommand extends CommandBase<BotSpamManager>
{
	public BotSpamListCommand(BotSpamManager plugin)
	{
		super(plugin, BotSpamManager.Perm.LIST_BOTSPAM_COMMAND, "list");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		List<SpamText> spamMessages = Plugin.getSpamTexts();

		if (spamMessages == null || spamMessages.isEmpty())
		{
			UtilPlayer.message(caller, F.main("BotSpam", "No botspam messages!"));
			return;
		}

		int totalPages = (int) Math.ceil(spamMessages.size() / 8.0);
		int page = 0;

		if (args.length != 0)
		{
			try
			{
				page = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException ex)
			{
				UtilPlayer.message(caller, F.main("BotSpam", F.elem(args[0]) + " is not a number!"));
				return;
			}
			page = page - 1;

			if (page < 0)
			{
				UtilPlayer.message(caller, F.main("BotSpam", "Page numbers must be greater than zero!"));
				return;
			}
			else if (page >= totalPages)
			{
				UtilPlayer.message(caller, F.main("BotSpam", "There are only " + F.elem(totalPages) + " pages of botspam messages, that number is too big!"));
				return;
			}
		}

		String header = "[" +
				ChatColor.RESET + C.cWhite + C.Bold + "BotSpam (" + (page + 1) + "/" + totalPages + ")" +
				ChatColor.RESET + C.cAqua + C.Strike + "]";

		int headerChars = ChatColor.stripColor(header).length();

		int numEqualsInHeader = (50 - headerChars) / 2;
		header = C.cAqua + C.Strike + StringUtils.repeat("=", numEqualsInHeader) + header + StringUtils.repeat("=", numEqualsInHeader);

		caller.sendMessage(header);

		int start = page * 8;

		List<SpamText> subList = start < spamMessages.size() ? spamMessages.subList(start, Math.min(spamMessages.size(), start + 8)) : Collections.emptyList();

		for (SpamText spamText : subList)
		{
			ComponentBuilder hover = new ComponentBuilder("")
					.append("Spam ID: ")
					.color(ChatColor.YELLOW)
					.append(spamText.getId() + "\n", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE)
					.append("Ban Count: ", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.YELLOW)
					.append(spamText.getPunishments() + "\n", ComponentBuilder.FormatRetention.NONE)
					.append("Enabled: ", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.YELLOW)
					.append((spamText.isEnabled() ? "True" : "False") + "\n", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE)
					.append("\n", ComponentBuilder.FormatRetention.NONE)
					.append("Created By: ", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.YELLOW)
					.append(spamText.getCreatedBy() + "\n", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE);

			if (spamText.getEnabledBy() != null)
			{
				hover
						.append("Enabled By: ", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.YELLOW)
						.append(spamText.getEnabledBy() + "\n", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.WHITE);
			}
			if (spamText.getDisabledBy() != null)
			{
				hover
						.append("Disabled By: ", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.YELLOW)
						.append(spamText.getDisabledBy() + "\n", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.WHITE);

			}
			hover.append("\n", ComponentBuilder.FormatRetention.NONE);

			if (spamText.isEnabled())
			{
				hover.append("Click to disable")
						.color(ChatColor.RED);
			}
			else
			{
				hover.append("Click to enable")
						.color(ChatColor.GREEN);
			}

			ComponentBuilder mainMsg = new ComponentBuilder("")
					.append(spamText.getText());

			if (!spamText.isEnabled())
			{
				mainMsg.color(ChatColor.RED);
			}

			mainMsg.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/botspam " + (spamText.isEnabled() ? "disable" : "enable") + " " + spamText.getId()));

			caller.spigot().sendMessage(mainMsg.create());
		}

		int chars = ChatColor.stripColor(header).length();

		int numEquals = (chars - 5) / 2; // 5 chars: " < > "

		ComponentBuilder pageSwitch = new ComponentBuilder("")
				.append(StringUtils.repeat("=", numEquals) + "[")
				.strikethrough(true)
				.color(ChatColor.AQUA)
				.append(" ", ComponentBuilder.FormatRetention.NONE)
				.append("<", ComponentBuilder.FormatRetention.NONE)
				.bold(true);

		if (page > 0)
		{
			BaseComponent[] prev = new ComponentBuilder("")
					.append("Go to page " + page)
					.color(ChatColor.GREEN)
					.create();

			pageSwitch
					.color(ChatColor.GREEN)
					.event(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/botspam list " + (page)))
					.event(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, prev));

		}
		else
		{
			pageSwitch
					.color(ChatColor.GRAY);
		}

		pageSwitch.append(" ", ComponentBuilder.FormatRetention.NONE)
				.append(">", ComponentBuilder.FormatRetention.NONE)
				.bold(true);

		if (page + 1 < totalPages)
		{
			BaseComponent[] next = new ComponentBuilder("")
					.append("Go to page " + (page + 2))
					.color(ChatColor.GREEN)
					.create();

			pageSwitch
					.color(ChatColor.GREEN)
					.event(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/botspam list " + (page + 2)))
					.event(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, next));

		}
		else
		{
			pageSwitch
					.color(ChatColor.GRAY);
		}

		pageSwitch
				.append(" ", ComponentBuilder.FormatRetention.NONE)
				.append("]" + StringUtils.repeat("=", numEquals), ComponentBuilder.FormatRetention.NONE)
				.strikethrough(true)
				.color(ChatColor.AQUA);

		caller.spigot().sendMessage(pageSwitch.create());
	}
}