package mineplex.core.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.updater.command.BuildVersionCommand;
import mineplex.core.updater.command.RestartServerCommand;
import mineplex.core.updater.event.RestartServerEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.RestartCommand;
import mineplex.serverdata.commands.ServerCommandManager;

public class FileUpdater extends MiniPlugin
{

	public enum Perm implements Permission
	{
		RESTART_COMMAND,
		BVERSION_COMMAND,
	}

	private static final FilenameFilter JAR_FILTER = (file, name) -> name.endsWith(".jar");

	private final Portal _portal;
	private final Map<String, String> _jarMd5Map = new ConcurrentHashMap<>();

	private final String _serverName;
	private final Region _region;
	private final GenericServer _transferHub;

	private AtomicBoolean _restartTriggered = new AtomicBoolean();
	private boolean _restartHappening, _enabled;

	private Properties _buildProperties;

	public FileUpdater(GenericServer transferHub)
	{
		super("Restart");

		_portal = Portal.getInstance();
		_serverName = UtilServer.getServerName();
		_region = UtilServer.getRegion();
		_transferHub = transferHub;

		getJarHashes();

		_enabled = !new File("IgnoreUpdates.dat").exists();

		ServerCommandManager.getInstance().registerCommandType(RestartCommand.class, command ->
		{
			if (_region != command.getRegion())
			{
				return;
			}

			if (command.isGroupRestart() ? _serverName.startsWith(command.getServerName()) : _serverName.equals(command.getServerName()))
			{
				RestartReason reason;
				int delay;

				if (command.isGroupRestart())
				{
					reason = RestartReason.GROUP_COMMAND;
					// If we are restarting the entire group then delay the shutdown to sometime within the next 5 minutes.
					// Prevents players getting fully kicked from the network.
					delay = UtilMath.r(300);
				}
				else
				{
					reason = RestartReason.SINGLE_COMMAND;
					delay = 0;
				}

				attemptToRestart(reason, delay);
			}
		});

		loadBuildProperties();

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.QA.setPermission(Perm.BVERSION_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.BVERSION_COMMAND, true, true);

		PermissionGroup.ADMIN.setPermission(Perm.RESTART_COMMAND, true, true);
		PermissionGroup.QAM.setPermission(Perm.RESTART_COMMAND, false, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new RestartServerCommand(this));
		addCommand(new BuildVersionCommand(this));
	}

	// This scares me, I don't know if it is used but I'm not removing it.
	@EventHandler
	public void onServerCommand(ServerCommandEvent event)
	{
		if (event.getCommand().startsWith("updatekickall "))
		{
			attemptToRestart(RestartReason.GROUP_COMMAND, 0);
		}
	}

	private void attemptToRestart(RestartReason reason, int delayInSeconds)
	{
		// If we already have a scheduled restart, don't try again.
		if (_restartTriggered.get())
		{
			return;
		}

		log("Restart scheduled by " + reason + ", delaying " + delayInSeconds + " seconds.");
		_restartTriggered.set(true);

		runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				// Keep calling this event until it doesn't get cancelled.
				// For example, for unimportant updates game servers don't need to restart until their current game is over.
				RestartServerEvent restartEvent = new RestartServerEvent(reason);
				UtilServer.CallEvent(restartEvent);

				if (restartEvent.isCancelled())
				{
					return;
				}

				_restartHappening = true;

				// Cancel the task
				cancel();

				// Tell the players
				Bukkit.broadcastMessage(F.main(getName(), F.color(_serverName, C.cGold) + " " + reason.getDescription()));

				// Move all players out and shutdown the server
				runSyncLater(() -> _portal.sendAllPlayersToGenericServer(_transferHub, Intent.KICK), 60L);
				runSyncLater(() -> Bukkit.getServer().shutdown(), 100L);
			}
		}, delayInSeconds * 20, 16 * 20);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void reflectMotd(ServerListPingEvent event)
	{
		if (_restartHappening)
		{
			event.setMotd("Restarting");
		}
	}

	private void checkForUpdates()
	{
		boolean windows = System.getProperty("os.name").startsWith("Windows");

		File updateDir = new File((windows ? "C:" : File.separator + "home" + File.separator + "mineplex") + File.separator + "update");
		File[] files = updateDir.listFiles(JAR_FILTER);

		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			String hash = _jarMd5Map.get(file.getName());
			if (hash != null)
			{
				try (FileInputStream stream = new FileInputStream(file))
				{
					String newHash = DigestUtils.md5Hex(stream);
					if (!hash.equals(newHash))
					{
						log(file.getName() + " old hash : " + hash);
						log(file.getName() + " new hash : " + newHash);
						attemptToRestart(RestartReason.JAR_UPDATE, 0);
					}
				}
				catch (IOException ex)
				{
					System.err.println("Failed to parse hash for file: " + file.getName() + ":");
					ex.printStackTrace();
				}
			}
		}
	}

	@EventHandler
	public void checkForUpdates(UpdateEvent event)
	{
		if (event.getType() != UpdateType.MIN_01 || !_enabled || _restartTriggered.get())
		{
			return;
		}

		runAsync(this::checkForUpdates);
	}

	private void collectHashes(File[] files)
	{
		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			try (FileInputStream stream = new FileInputStream(file))
			{
				String fileName = file.getName().replace(UtilServer.getServerName(), "craftbukkit");
				_jarMd5Map.put(fileName, DigestUtils.md5Hex(stream));
			}
			catch (IOException ex)
			{
				System.err.println("Failed to parse hash for file: " + file.getName() + ":");
				ex.printStackTrace();
			}
		}
	}

	private void getJarHashes()
	{
		File base = new File(".");
		collectHashes(base.listFiles(JAR_FILTER));

		File plugins = new File("plugins");
		collectHashes(plugins.listFiles(JAR_FILTER));
	}

	private void loadBuildProperties()
	{
		_buildProperties = new Properties();

		try
		{
			_buildProperties.load(this.getClass().getResourceAsStream("/version.properties"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Properties getBuildProperties()
	{
		return _buildProperties;
	}

	public Region getRegion()
	{
		return _region;
	}
}