package mineplex.core.party.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.command.cli.PartyAcceptCommand;
import mineplex.core.party.command.cli.PartyDenyCommand;
import mineplex.core.party.command.cli.PartyDisbandCommand;
import mineplex.core.party.command.cli.PartyHelpCommand;
import mineplex.core.party.command.cli.PartyInviteCommand;
import mineplex.core.party.command.cli.PartyInvitesCommand;
import mineplex.core.party.command.cli.PartyKickCommand;
import mineplex.core.party.command.cli.PartyLeaveCommand;
import mineplex.core.party.command.cli.PartyTransferOwnerCommand;

public class PartyCLICommand extends MultiCommandBase<PartyManager>
{
	public PartyCLICommand(PartyManager plugin)
	{
		super(plugin, PartyManager.Perm.PARTY_COMMAND, "cli", "c");

		AddCommand(new PartyAcceptCommand(plugin));
		AddCommand(new PartyDenyCommand(plugin));
		AddCommand(new PartyDisbandCommand(plugin));
		AddCommand(new PartyInviteCommand(plugin));
		AddCommand(new PartyInvitesCommand(plugin));
		AddCommand(new PartyKickCommand(plugin));
		AddCommand(new PartyLeaveCommand(plugin));
		AddCommand(new PartyTransferOwnerCommand(plugin));
		AddCommand(new PartyHelpCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		Party party = Plugin.getPartyByPlayer(caller);

		if (args.length > 0)
		{
			UtilPlayer.message(caller, F.main("Party", "That is not a valid command! Try " + F.elem("/party help") + "!"));
			return;
		}

		if (party == null)
		{
			UtilPlayer.message(caller, F.main("Party", "You're not in a party! Try " + F.elem("/party help") + "!"));
			return;
		}

		ComponentBuilder builder = new ComponentBuilder("")
				.append("[", ComponentBuilder.FormatRetention.NONE)
				.bold(true)
				.color(ChatColor.AQUA)
				.append("?", ComponentBuilder.FormatRetention.NONE)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to view Party Help").create()))
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/z help"))
				.append("]", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.AQUA)
				.bold(true)
				.append("===========", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.AQUA)
				.bold(true)
				.strikethrough(true)
				.append("[", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.AQUA)
				.bold(true)
				.append("Your Party", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.WHITE)
				.bold(true)
				.append("]", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.AQUA)
				.bold(true)
				.append("==============", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.AQUA)
				.bold(true)
				.strikethrough(true)
				.append("\n\n", ComponentBuilder.FormatRetention.NONE);

		builder.append("Leader")
				.color(party.getOwnerAsPlayer() == null ? ChatColor.GRAY : ChatColor.LIGHT_PURPLE)
				.bold(true)
				.append(" ", ComponentBuilder.FormatRetention.NONE)
				.append(party.getOwnerName())
				.append("\n");

		for (Player member : party.getMembers())
		{
			if (member.getUniqueId().equals(party.getOwner().getId()))
				continue;

			builder.append("Member")
					.color(ChatColor.DARK_PURPLE)
					.bold(true)
					.append(" ", ComponentBuilder.FormatRetention.NONE)
					.append(member.getName());

			if (party.isOwner(caller))
			{
				builder.append(" ", ComponentBuilder.FormatRetention.NONE)
						.append("✕", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.RED)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Kick " + member.getName() + " from your party").color(ChatColor.RED).create()))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/z cli kick " + member.getName()))
						.append(" ", ComponentBuilder.FormatRetention.NONE)
						.append("⬆", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.GOLD)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Transfer party to " + member.getName()).color(ChatColor.GOLD).create()))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/z cli tr " + member.getName()));
			}

			builder.append("\n", ComponentBuilder.FormatRetention.NONE);
		}

		builder.append("\n", ComponentBuilder.FormatRetention.NONE);

		builder.append("Toggle GUI")
				.color(ChatColor.GREEN)
				.bold(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Switch to the chest GUI instead of chat").color(ChatColor.GREEN).create()))
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/z t"))
				.append(" - ", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.GRAY)
				.append("Leave")
				.color(ChatColor.RED)
				.bold(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Leave your party").color(ChatColor.RED).create()))
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/z cli leave"));

		if (party.isOwner(caller))
		{
			builder
					.append(" - ")
					.color(ChatColor.GRAY)
					.append("Disband")
					.color(ChatColor.DARK_RED)
					.bold(true)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Disband your party").color(ChatColor.DARK_RED).create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/z cli disband"));
		}

		builder.append("\n", ComponentBuilder.FormatRetention.NONE)
				.append("======================================")
				.color(ChatColor.AQUA)
				.bold(true)
				.strikethrough(true);

		caller.spigot().sendMessage(builder.create());
	}
}