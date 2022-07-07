package mineplex.core.teamspeak;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.teamspeak.commands.TeamspeakCommand;
import mineplex.core.teamspeak.redis.TeamspeakLinkRequest;
import mineplex.core.teamspeak.redis.TeamspeakLinkResponse;
import mineplex.core.teamspeak.redis.TeamspeakUnlinkRequest;
import mineplex.core.teamspeak.redis.TeamspeakUnlinkResponse;
import mineplex.serverdata.commands.ServerCommandManager;

@ReflectivelyCreateMiniPlugin
public class TeamspeakManager extends MiniClientPlugin<TeamspeakClientInfo> implements ILoginProcessor
{
	public enum Perm implements Permission
	{
		LINK_COMMAND,
		LIST_COMMAND,
		TEAMSPEAK_COMMAND,
		UNLINK_COMMAND,
	}
	
	public static final String TEAMSPEAK_CHANNEL_NAME = "Teamspeak Rank Channel";

	public static final int MAX_LINKED_ACCOUNTS = 5;

	private final CoreClientManager _clientManager = require(CoreClientManager.class);
	private final TeamspeakRepository _repository = new TeamspeakRepository();

	private final Map<UUID, BukkitTask> _requestMap = new HashMap<>();

	private TeamspeakManager()
	{
		super("TeamSpeak Manager");

		ServerCommandManager.getInstance().registerCommandType(TeamspeakLinkResponse.class, response ->
		{
			BukkitTask task = _requestMap.remove(response.getRequest().getCommandId());

			if (task == null)
				return;

			task.cancel();

			Player sender = Bukkit.getPlayer(response.getRequest().getCaller());

			if (sender == null)
				return;

			switch (response.getResponse())
			{
				case TOKEN_VALID:
					Date now = new Date();
					Get(sender).link(response.getId(), now);
					runAsync(() ->
					{
						_repository.save(_clientManager.getAccountId(sender), response.getId(), now);
					});

					UtilPlayer.message(sender, F.main("Teamspeak", "Congrats! You've successfully linked your Teamspeak account!"));
					break;
				case TOKEN_INVALID:
					UtilPlayer.message(sender, F.main("Teamspeak", "Uh oh! That's not the token you were given!"));
					break;
			}
		});

		ServerCommandManager.getInstance().registerCommandType(TeamspeakUnlinkResponse.class, response ->
		{
			BukkitTask task = _requestMap.remove(response.getRequest().getCommandId());

			if (task == null)
				return;

			task.cancel();

			Player sender = Bukkit.getPlayer(response.getRequest().getCaller());

			if (sender == null)
				return;

			switch (response.getResponse())
			{
				case UNLINKED:
					Get(sender).unlink(response.getRequest().getId());
					runAsync(() ->
					{
						_repository.delete(_clientManager.getAccountId(sender), response.getRequest().getId());
					});

					UtilPlayer.message(sender, F.main("Teamspeak", "You've unlinked your Teamspeak account!"));
					break;
			}
		});

		_clientManager.addStoredProcedureLoginProcessor(this);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.LINK_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.LIST_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TEAMSPEAK_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UNLINK_COMMAND, true, true);
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Map<Integer, Date> dates = new HashMap<>();

		while (resultSet.next())
		{
			int teamspeakId = resultSet.getInt("teamspeakId");
			Date linkDate = resultSet.getDate("linkDate");

			dates.put(teamspeakId, linkDate);
		}

		Set(uuid, new TeamspeakClientInfo(dates));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return String.format(TeamspeakRepository.GET_ALL_PLAYER_IDS, accountId);
	}

	@Override
	protected TeamspeakClientInfo addPlayer(UUID uuid)
	{
		return new TeamspeakClientInfo(Collections.emptyMap());
	}

	public void link(Player caller, String token)
	{
		if (_requestMap.containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "Please wait until your current request has been processed"));
			return;
		}

		TeamspeakClientInfo info = Get(caller);

		if (info.getLinkedAccounts().size() >= MAX_LINKED_ACCOUNTS)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "You have too many linked accounts! Please unlink one using /teamspeak unlink"));
			return;
		}

		UtilPlayer.message(caller, F.main("Teamspeak", "Linking your account..."));

		TeamspeakLinkRequest linkRequest = new TeamspeakLinkRequest(
				caller.getUniqueId(),
				token
		);
		linkRequest.publish();

		_requestMap.put(linkRequest.getCommandId(), runSyncLater(() ->
		{
			if (!caller.isOnline())
				return;

			UtilPlayer.message(caller, F.main("Teamspeak", "It seems something has gone wrong - your request couldn't be processed"));

			_requestMap.remove(linkRequest.getCommandId());
		}, 20L * 5));
	}

	public void unlink(Player caller, String arg)
	{
		int id;
		try
		{
			id = Integer.parseInt(arg);
		}
		catch (NumberFormatException ex)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", F.elem(arg) + " is not a number"));
			return;
		}

		if (_requestMap.containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "Please wait until your current request has been processed"));
			return;
		}

		TeamspeakClientInfo info = Get(caller);

		if (!info.getLinkedAccounts().containsKey(id))
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "You have not linked to that account"));
			return;
		}

		UtilPlayer.message(caller, F.main("Teamspeak", "Unlinking your account..."));

		TeamspeakUnlinkRequest unlinkRequest = new TeamspeakUnlinkRequest(
				caller.getUniqueId(),
				id
		);
		unlinkRequest.publish();

		_requestMap.put(unlinkRequest.getCommandId(), runSyncLater(() ->
		{
			if (!caller.isOnline())
				return;

			UtilPlayer.message(caller, F.main("Teamspeak", "It seems something has gone wrong - your request couldn't be processed"));

			_requestMap.remove(unlinkRequest.getCommandId());
		}, 20L * 5));
	}

	@Override
	public void addCommands()
	{
		addCommand(new TeamspeakCommand(this));
	}

	public void displayUnlinkPrompt(Player caller, String strPage)
	{
		TeamspeakClientInfo info = Get(caller);

		List<Map.Entry<Integer, Date>> linkedAccounts = new ArrayList<>(info.getLinkedAccounts().entrySet());

		if (linkedAccounts.size() == 0)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "You have no linked Teamspeak accounts!"));
			return;
		}

		int totalPages = (int) Math.ceil(linkedAccounts.size() / 8.0);
		int page;

		try
		{
			page = Integer.parseInt(strPage);
		}
		catch (NumberFormatException ex)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", F.elem(strPage) + " is not a number!"));
			return;
		}
		page = page - 1;

		if (page < 0)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "Page numbers must be greater than zero!"));
			return;
		}
		else if (page >= totalPages)
		{
			UtilPlayer.message(caller, F.main("Teamspeak", "You only have " + F.elem(totalPages) + " pages of linked accounts, that number is too big!"));
			return;
		}

		String header = "[" +
				ChatColor.RESET + C.cWhite + C.Bold + "Teamspeak Accounts (" + (page + 1) + "/" + totalPages + ")" +
				ChatColor.RESET + C.cAqua + C.Strike + "]";

		int headerChars = ChatColor.stripColor(header).length();

		int numEqualsInHeader = (50 - headerChars) / 2;
		header = C.cAqua + C.Strike + StringUtils.repeat("=", numEqualsInHeader) + header + StringUtils.repeat("=", numEqualsInHeader);

		caller.sendMessage(header);

		int start = page * 8;

		List<Map.Entry<Integer, Date>> subList = start < linkedAccounts.size() ? linkedAccounts.subList(start, Math.min(linkedAccounts.size(), start + 8)) : Collections.emptyList();

		for (Map.Entry<Integer, Date> data : subList)
		{
			BaseComponent[] hover = new ComponentBuilder("")
					.append("ID: ")
					.color(ChatColor.YELLOW)
					.append(String.valueOf(data.getKey()), ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE)
					.create();

			ComponentBuilder builder = new ComponentBuilder("")
					.append("Unlink")
					.color(ChatColor.RED)
					.bold(true)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink this account. All your ranks on Teamspeak will be removed").color(ChatColor.RED).create()))
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teamspeak unlink " + data.getKey()))
					.append(" - ", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.WHITE)
					.append("Account linked on " + UtilTime.date(data.getValue().getTime()))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
					.color(ChatColor.GRAY);

			caller.spigot().sendMessage(builder.create());
		}

		int chars = ChatColor.stripColor(header).length();

		int numEquals = (chars - 3) / 2;

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
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teamspeak list " + (page)))
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
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teamspeak list " + (page + 2)))
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