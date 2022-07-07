package mineplex.mapparser.module.modules;

import com.google.common.collect.Maps;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.command.BaseCommand;
import mineplex.mapparser.module.Module;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;

/**
 *
 */
public class CommandModule extends Module
{
	private Map<String, BaseCommand> _commands = Maps.newHashMap();
	private Map<String, BaseCommand> _commandsByAlias = Maps.newHashMap();

	public CommandModule(MapParser plugin)
	{
		super("Commands", plugin);
	}

	@EventHandler
	public void preventMe(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().toLowerCase().startsWith("/bukkit")
				|| event.getMessage().toLowerCase().startsWith("/minecraft"))
		{
			if (!event.getPlayer().isOp())
			{
				event.getPlayer().sendMessage(F.main("Parser", "Nope, not allowed!"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();

		String[] parts = event.getMessage().split(" ");
		String commandLabel = parts[0].substring(1);
		String[] args = new String[parts.length - 1];
		System.arraycopy(parts, 1, args, 0, parts.length - 1);

		if (getPlugin().getCurParse() != null)
		{
			UtilPlayerBase.message(player, F.main("Parser", "Cannot use commands during Map Parse!"));
			return;
		}
		if (event.getMessage().toLowerCase().startsWith("/help"))
		{
			event.setCancelled(true);

			displayHelp(player);
			return;
		}

		if (commandLabel.equalsIgnoreCase("gmc"))
		{
			player.setGameMode(GameMode.CREATIVE);
			event.setCancelled(true);
			return;
		}
		if (commandLabel.equalsIgnoreCase("gms"))
		{
			player.setGameMode(GameMode.SURVIVAL);
			event.setCancelled(true);
			return;
		}
		if (commandLabel.equalsIgnoreCase("gmsp"))
		{
			player.setGameMode(GameMode.SPECTATOR);
			event.setCancelled(true);
			return;
		}

		BaseCommand baseCommand = _commands.get(commandLabel.toLowerCase());

		if (baseCommand == null)
		{
			baseCommand = _commandsByAlias.get(commandLabel.toLowerCase());
			if (baseCommand == null)
			{
				return;
			}
		}

		event.setCancelled(true);

		if (!baseCommand.canRun(player))
		{
			player.sendMessage(F.main(getPlugin().getName(), "You don't have permission to do that."));
			return;
		}

		if (!baseCommand.execute(player, commandLabel, args))
		{
			UtilPlayerBase.message(player, F.main("Parser", "Invalid Input."));
			UtilPlayerBase.message(player, F.elem(baseCommand.getUsage()));
		}
	}

	public void add(BaseCommand baseCommand)
	{
		_commands.put(baseCommand.getAliases().get(0).toLowerCase(), baseCommand);
		for (String label : baseCommand.getAliases())
		{
			_commandsByAlias.put(label.toLowerCase(), baseCommand);
		}
	}

	public Map<String, BaseCommand> getCommands()
	{
		return _commands;
	}
}
