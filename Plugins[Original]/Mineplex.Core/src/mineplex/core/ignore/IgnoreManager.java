package mineplex.core.ignore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.jsonchat.ChildJsonMessage;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.ignore.command.Ignore;
import mineplex.core.ignore.command.Unignore;
import mineplex.core.ignore.data.IgnoreData;
import mineplex.core.ignore.data.IgnoreRepository;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;

public class IgnoreManager extends MiniDbClientPlugin<IgnoreData>
{
	public enum Perm implements Permission
	{
		IGNORE_COMMAND,
		BYPASS_IGNORE,
	}

	private PreferencesManager _preferenceManager;
	private IgnoreRepository _repository;
	private Portal _portal;

	public IgnoreManager(JavaPlugin plugin, CoreClientManager clientManager, PreferencesManager preferences, Portal portal)
	{
		super("Ignore", plugin, clientManager);

		_preferenceManager = preferences;
		_repository = new IgnoreRepository(plugin);
		_portal = portal;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.IGNORE_COMMAND, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.BYPASS_IGNORE, true, true);
	}

	public PreferencesManager getPreferenceManager()
	{
		return _preferenceManager;
	}

	public Portal getPortal()
	{
		return _portal;
	}

	public boolean isIgnoring(Player caller, Player target)
	{
		return isIgnoring(caller, target.getName());
	}

	public boolean isIgnoring(Player caller, String target)
	{
		IgnoreData data = Get(caller);

		for (String ignored : data.getIgnored())
		{
			if (ignored.equalsIgnoreCase(target))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void addCommands()
	{
		addCommand(new Ignore(this));
		addCommand(new Unignore(this));
	}

	@Override
	protected IgnoreData addPlayer(UUID uuid)
	{
		return new IgnoreData();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event)
	{
		if (ClientManager.Get(event.getPlayer()).hasPermission(Perm.BYPASS_IGNORE))
		{
			return;
		}

		Iterator<Player> itel = event.getRecipients().iterator();

		while (itel.hasNext())
		{
			Player player = itel.next();

			IgnoreData info = Get(player);

			for (String ignored : info.getIgnored())
			{
				if (ignored.equalsIgnoreCase(event.getPlayer().getName()))
				{
					itel.remove();

					break;
				}
			}
		}
	}

	public void addIgnore(final Player caller, final String name)
	{
		if (caller.getName().equalsIgnoreCase(name))
		{
			caller.sendMessage(F.main(getName(), ChatColor.GRAY + "You cannot ignore yourself"));
			return;
		}

		for (String status : Get(caller).getIgnored())
		{
			if (status.equalsIgnoreCase(name))
			{
				caller.sendMessage(F.main(getName(), ChatColor.GREEN + name + ChatColor.GRAY + " has already been ignored."));
				return;
			}
		}

		IgnoreData ignoreData = Get(caller);

		if (ignoreData != null)
		{
			ignoreData.getIgnored().add(name);
		}

		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), () ->
		{
			_repository.addIgnore(caller, name);

			Bukkit.getServer().getScheduler().runTask(_plugin, () ->
			{
				caller.sendMessage(F.main(getName(), "Now ignoring " + ChatColor.GREEN + name));
			});
		});
	}

	public void removeIgnore(final Player caller, final String name)
	{
		IgnoreData ignoreData = Get(caller);

		if (ignoreData != null)
		{
			Iterator<String> itel = ignoreData.getIgnored().iterator();

			while (itel.hasNext())
			{
				String ignored = itel.next();

				if (ignored.equalsIgnoreCase(name))
				{
					itel.remove();
					break;
				}
			}
		}

		caller.sendMessage(F.main(getName(), "No longer ignoring " + ChatColor.GREEN + name + ChatColor.GRAY + "!"));

		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), () ->
		{
			_repository.removeIgnore(caller.getName(), name);
		});
	}

	public void showIgnores(Player caller)
	{
		List<String> ignoredPlayers = Get(caller).getIgnored();

		caller.sendMessage(C.cAqua + C.Strike + "=====================[" + ChatColor.RESET + C.cWhite + C.Bold + "Ignoring"
				+ ChatColor.RESET + C.cAqua + C.Strike + "]======================");

		List<ChildJsonMessage> sentLines = new ArrayList<>();

		for (String ignored : ignoredPlayers)
		{

			ChildJsonMessage message = new JsonMessage("").color("white").extra("").color("white");

			message.add("Ignoring " + ignored).color("gray");

			message.add(" - ").color("white");

			message.add("Unignore").color("red").bold().click("run_command", "/unignore " + ignored)
				.hover("show_text", "Stop ignoring " + ignored);

			sentLines.add(message);
		}

		// Send In Order
		for (JsonMessage msg : sentLines)
			msg.sendToPlayer(caller);

		if (sentLines.isEmpty())
		{
			caller.sendMessage(" ");
			caller.sendMessage("Welcome to your Ignore List!");
			caller.sendMessage(" ");
			caller.sendMessage("To ignore people, type " + C.cGreen + "/ignore <Player Name>");
			caller.sendMessage(" ");
			caller.sendMessage("Type " + C.cGreen + "/ignore" + ChatColor.RESET + " at any time to view the ignored!");
			caller.sendMessage(" ");
		}

		ChildJsonMessage message = new JsonMessage("").extra(C.cAqua + C.Strike
				+ "=====================================================");

		message.sendToPlayer(caller);
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(uuid, _repository.loadClientInformation(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT tA.Name FROM accountIgnore INNER Join accounts AS fA ON fA.uuid = uuidIgnorer INNER JOIN accounts AS tA ON tA.uuid = uuidIgnored LEFT JOIN playerMap ON tA.name = playerName WHERE uuidIgnorer = '"
				+ uuid + "';";
	}
}