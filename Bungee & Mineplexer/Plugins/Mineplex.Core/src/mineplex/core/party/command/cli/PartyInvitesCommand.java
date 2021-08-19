package mineplex.core.party.command.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.InviteData;
import mineplex.core.party.PartyManager;

public class PartyInvitesCommand extends CommandBase<PartyManager>
{
	private static int boostCount = 0;

	public PartyInvitesCommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "invites", "is");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 2 && args[0].equals("boost"))
		{
			boostCount = Integer.parseInt(args[1]);
			return;
		}

		List<InviteData> invites = Plugin.getInviteManager().getAllInvites(caller);

		invites.sort(Comparator.comparing(InviteData::getInviterName));

		if (boostCount != 0)
		{
			invites = new ArrayList<>();

			for (int i = 0; i < boostCount; i++)
			{
				invites.add(new InviteData("Player" + i, UUID.randomUUID(), UUID.randomUUID(), "Server" + i));
			}
		}

		if (invites == null || invites.isEmpty())
		{
			UtilPlayer.message(caller, F.main("Party", "You have no pending invites!"));
			return;
		}

		int totalPages = (int) Math.ceil(invites.size() / 8.0);
		int page = 0;

		if (args.length != 0)
		{
			try
			{
				page = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException ex)
			{
				UtilPlayer.message(caller, F.main("Party", F.elem(args[0]) + " is not a number!"));
				return;
			}
			page = page - 1;

			if (page < 0)
			{
				UtilPlayer.message(caller, F.main("Party", "Page numbers must be greater than zero!"));
				return;
			}
			else if (page >= totalPages)
			{
				UtilPlayer.message(caller, F.main("Party", "You only have " + F.elem(totalPages) + " pages of invites, that number is too big!"));
				return;
			}
		}

		String header = "[" +
				ChatColor.RESET + C.cWhite + C.Bold + "Party Invites (" + (page + 1) + "/" + totalPages + ")" +
				ChatColor.RESET + C.cAqua + C.Strike + "]";

		int headerChars = ChatColor.stripColor(header).length();

		int numEqualsInHeader = (50 - headerChars) / 2;
		header = C.cAqua + C.Strike + StringUtils.repeat("=", numEqualsInHeader) + header + StringUtils.repeat("=", numEqualsInHeader);

		caller.sendMessage(header);

		int start = page * 8;

		List<InviteData> subList = start < invites.size() ? invites.subList(start, Math.min(invites.size(), start + 8)) : Collections.emptyList();

		for (InviteData data : subList)
		{
			BaseComponent[] hover = new ComponentBuilder("")
					.append("Server: ")
					.color(ChatColor.YELLOW)
					.append(data.getServerName(), ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE)
					.create();

			ComponentBuilder builder = new ComponentBuilder("")
					.append("Accept")
					.color(ChatColor.GREEN)
					.bold(true)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to accept this invite").color(ChatColor.GREEN).create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party cli a " + data.getInviterName()))
					.append(" - ", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE)
					.append("Deny")
					.color(ChatColor.RED)
					.bold(true)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to deny this invite").color(ChatColor.RED).create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party cli d " + data.getInviterName()))
					.append(" - ", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE)
					.append(data.getInviterName() + " invited you to their party")
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
					.color(ChatColor.GRAY);

			caller.spigot().sendMessage(builder.create());
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
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party is " + (page)))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, prev));

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
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party is " + (page + 2)))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, next));

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