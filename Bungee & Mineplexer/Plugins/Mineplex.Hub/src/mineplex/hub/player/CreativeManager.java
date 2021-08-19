package mineplex.hub.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.hub.player.command.CreativeCommand;

@ReflectivelyCreateMiniPlugin
public class CreativeManager extends MiniPlugin
{

	private final Map<Player, List<String>> _creativeAdmin;

	private CreativeManager()
	{
		super("Creative");

		_creativeAdmin = new HashMap<>();
	}

	@Override
	public void addCommands()
	{
		addCommand(new CreativeCommand(this));
	}

	public void addGameMode(Player caller, Player target)
	{
		_creativeAdmin.putIfAbsent(caller, new ArrayList<>());

		List<String> creative = _creativeAdmin.get(caller);

		if (target.getGameMode() == GameMode.CREATIVE)
		{
			creative.add(target.getName());
		}
		else
		{
			creative.remove(target.getName());
		}
	}

	public boolean isInCreative(Player player)
	{
		String name = player.getName();

		for (List<String> players : _creativeAdmin.values())
		{
			if (players.contains(name))
			{
				return true;
			}
		}

		return false;
	}

	@EventHandler
	public void clearGameMode(PlayerQuitEvent event)
	{
		List<String> creative = _creativeAdmin.remove(event.getPlayer());

		if (creative == null)
		{
			return;
		}

		for (String name : creative)
		{
			Player player = UtilPlayer.searchExact(name);

			if (player == null)
			{
				continue;
			}

			player.setGameMode(GameMode.ADVENTURE);
			UtilPlayer.message(player, F.main(_moduleName, event.getPlayer().getName() + " left the game. Creative Mode: " + F.tf(false)));
		}
	}
}
