package mineplex.core.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;
import net.minecraft.server.v1_8_R3.PacketPlayOutDeclareCommands;
import net.minecraft.server.v1_8_R3.PacketPlayOutTabComplete;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.mineplex.ProtocolVersion;

import mineplex.core.Managers;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.core.common.util.UtilServer;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.recharge.Recharge;

public class CommandCenter implements Listener, IPacketHandler
{
	public static final List<String> ALLOW_SPAM_IF_LAST = new ArrayList<>();
	public static CommandCenter Instance;

	protected JavaPlugin Plugin;
	protected CoreClientManager ClientManager;
	protected static NautHashMap<String, ICommand> Commands;
	private final List<String> BLOCKED_COMMANDS = Lists.newArrayList("pl", "plugins", "ver", "version", "icanhasbukkit", "about");
	private final String MESSAGE = C.cRed + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.";

	private final PacketHandler _packetHandler = Managers.require(PacketHandler.class);

	private static AtomicIntegerFieldUpdater<PlayerConnection> chatSpamField = null;

	private Map<UUID, String> _playerLastCommand = new HashMap<>();

	public enum Perm implements Permission
	{
		BLOCKED_COMMAND,
	}

	static
	{
		try
		{
			Field field = PlayerConnection.class.getDeclaredField("chatSpamField");
			field.setAccessible(true);
			chatSpamField = (AtomicIntegerFieldUpdater<PlayerConnection>) field.get(null);
		}
		catch (ReflectiveOperationException ex)
		{
			throw new RuntimeException("An unexpected error occured while reflectively accessing a field", ex);
		}
	}

	public static void Initialize(JavaPlugin plugin)
	{
		if (Instance == null)
			Instance = new CommandCenter(plugin);
	}

	public CoreClientManager GetClientManager()
	{
		return ClientManager;
	}

	private CommandCenter(JavaPlugin instance)
	{
		Plugin = instance;
		Commands = new NautHashMap<>();
		Plugin.getServer().getPluginManager().registerEvents(this, Plugin);

		_packetHandler.addPacketHandler(this, true, PacketPlayInTabComplete.class);

		PermissionGroup.DEV.setPermission(Perm.BLOCKED_COMMAND, true, true);
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAM.setPermission(Perm.BLOCKED_COMMAND, false, true);
		}
	}

	public void setClientManager(CoreClientManager clientManager)
	{
		ClientManager = clientManager;
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		String commandName = event.getMessage().substring(1);
		String argString = event.getMessage().substring(event.getMessage().indexOf(' ') + 1);
		String[] args = new String[]{};

		if (commandName.contains(" "))
		{
			commandName = commandName.split(" ")[0];
			args = argString.split(" ");
		}

		String commandLabel = commandName.toLowerCase();

		ICommand command = Commands.get(commandLabel);

		if (command != null)
		{
			event.setCancelled(true);

			if (ClientManager.Get(event.getPlayer()).hasPermission(command.getPermission())
					|| UtilPlayer.isCommandAllowed(event.getPlayer(), commandName.toLowerCase()))
			{
				// Disallow the player to spam if they have sent a command in the last 500ms,
				if (!Recharge.Instance.use(event.getPlayer(), "Command", 500, false, false))
				{
					// They must have not sent a command yet,
					// or their command is not part of the whitelisted for spamming
					// and they didn't last send the same one (to prevent infinite chains)
					String lastCommand = _playerLastCommand.get(event.getPlayer().getUniqueId());

					if (lastCommand == null || (!ALLOW_SPAM_IF_LAST.contains(lastCommand) && !lastCommand.equalsIgnoreCase(commandLabel)))
					{
						event.getPlayer().sendMessage(F.main("Command Center", "You can't spam commands that fast."));
						return;
					}
				}

				_playerLastCommand.put(event.getPlayer().getUniqueId(), commandLabel);

				command.SetAliasUsed(commandLabel);
				
				if (command instanceof LoggedCommand)
				{
					((LoggedCommand) command).execute(System.currentTimeMillis(), event.getPlayer().getName(), commandName, argString);
				}
				
				command.Execute(event.getPlayer(), args);
			}
			else
			{
				UtilPlayerBase.message(event.getPlayer(), C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			}
			return;
		}

		if (BLOCKED_COMMANDS.contains(commandName.toLowerCase()) && !(event.getPlayer().isOp() || ClientManager.Get(event.getPlayer()).hasPermission(Perm.BLOCKED_COMMAND)))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(MESSAGE);
			return;
		}
	}

	public void addCommand(ICommand command)
	{
		for (String commandRoot : command.Aliases())
		{
			Commands.put(commandRoot.toLowerCase(), command);
			command.SetCommandCenter(this);
		}
	}

	public void removeCommand(ICommand command)
	{
		for (String commandRoot : command.Aliases())
		{
			Commands.remove(commandRoot.toLowerCase());
			command.SetCommandCenter(null);
		}
	}

	public static NautHashMap<String, ICommand> getCommands()
	{
		return Commands;
	}

	private List<String> getCommands(Player player)
	{
		CoreClient client = ClientManager.Get(player);
		List<String> commands = new ArrayList<>();
		for (Map.Entry<String, ICommand> entry : Commands.entrySet())
		{
			if (client.hasPermission(entry.getValue().getPermission()))
			{
				commands.add(entry.getKey());
			}
		}

		return commands;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		int protocol = UtilPlayer.getProtocol(event.getPlayer());
		if (protocol >= ProtocolVersion.v1_13)
		{
			List<String> commands = getCommands(event.getPlayer());
			UtilPlayer.sendPacket(event.getPlayer(), new PacketPlayOutDeclareCommands(commands));
		}
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayInTabComplete)
		{
			EntityPlayer nmsPlayer = ((CraftPlayer) packetInfo.getPlayer()).getHandle();
			if (nmsPlayer.getProtocol() >= ProtocolVersion.v1_13)
			{
				return;
			}

			PacketPlayInTabComplete packet = (PacketPlayInTabComplete) packetInfo.getPacket();

			String message = packet.a();
			if (message.startsWith("/"))
			{
				packetInfo.setCancelled(true);

				PlayerConnection playerConnection = nmsPlayer.playerConnection;
				if (chatSpamField.addAndGet(playerConnection, 10) > 500 && !packetInfo.getPlayer().isOp())
				{
					playerConnection.disconnect("disconnect.spam");
					return;
				}

				Set<String> results = new HashSet<>();

				String commandName = message.substring(1);
				String[] args = new String[0];

				if (commandName.contains(" "))
				{
					String[] splits = commandName.split(" ", -1);
					commandName = splits[0];
					args = new String[splits.length - 1];
					System.arraycopy(splits, 1, args, 0, args.length);
				}

//				System.out.println("Handling tab complete for " + packetInfo.getPlayer().getName() + " " + commandName + " " + Arrays.toString(args) + " " + endsWithSpace);

				if (args.length > 0)
				{
//					System.out.println("Path 1");
					ICommand command = Commands.get(commandName.toLowerCase());

					if (command != null)
					{
						if (ClientManager.Get(packetInfo.getPlayer()).hasPermission(command.getPermission())
						    || UtilPlayer.isCommandAllowed(packetInfo.getPlayer(), commandName.toLowerCase()))
						{
							List<String> tmpres = command
									.onTabComplete(packetInfo.getPlayer(), commandName.toLowerCase(), args);
							if (tmpres != null)
							{
								results.addAll(tmpres);
							}
						}
					}
				}
				// tab complete commands?
				else
				{
//					System.out.println("Path 2");
					for (ICommand command : Commands.values())
					{
						if (ClientManager.Get(packetInfo.getPlayer()).hasPermission(command.getPermission())
						    || UtilPlayer.isCommandAllowed(packetInfo.getPlayer(), commandName.toLowerCase()))
						{
							for (String alias : command.Aliases())
							{
								if (alias.startsWith(commandName))
								{
									results.add("/" + alias.toLowerCase());
								}
							}
						}
					}
				}

//				System.out.println("Final results: " + results);

				playerConnection.sendPacket(new PacketPlayOutTabComplete(results.toArray(new String[0])));
			}
		}
	}
}