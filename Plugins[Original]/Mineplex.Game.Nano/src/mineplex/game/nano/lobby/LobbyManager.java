package mineplex.game.nano.lobby;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.NanoPlayer;

@ReflectivelyCreateMiniPlugin
public class LobbyManager extends GameManager
{

	private final MineplexWorld _mineplexWorld;
	private final Location _spawn;

	private final String[] _info =
			{
					"This game goes on forever! If you ever want to cash out on your rewards, you can do so at any time using the " + C.cGreen + "Cash Out Clock" + C.cPurple + " or " + C.cYellow + "/hub" + C.cPurple + "!",
					"Suggest new " + C.cGreen + "Nano Games" + C.cPurple + "! We're open to hearing your ideas and changes! Click " + C.cYellowB + "HERE" + C.cPurple + " to go to the forums and suggest them!",
			};
	private int _infoTick;

	private LobbyManager()
	{
		super("Lobby");

		_mineplexWorld = new MineplexWorld(Bukkit.getWorlds().get(0));
		_spawn = _mineplexWorld.getSpongeLocation("SPAWN");

		_mineplexWorld.getWorld().setSpawnLocation(_spawn.getBlockX(), _spawn.getBlockY(), _spawn.getBlockZ());
	}

	@EventHandler
	public void updateInfo(UpdateEvent event)
	{
		if (event.getType() != UpdateType.MIN_01)
		{
			return;
		}

		String info = C.cPurpleB + "INFO" + C.cDGrayB + "> " + C.cPurple + _info[_infoTick];
		_infoTick = (_infoTick + 1) % _info.length;

		BaseComponent[] components = TextComponent.fromLegacyText(info);
		HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to visit the forums.")
				.color(ChatColor.YELLOW)
				.create());
		ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://xen.mineplex.com/forums/nano-games.388/");

		for (BaseComponent component : components)
		{
			component.setHoverEvent(hoverEvent);
			component.setClickEvent(clickEvent);
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.spigot().sendMessage(components);
		}
	}

	@EventHandler
	public void updateWaiting(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWER || _manager.getGame() != null)
		{
			return;
		}

		UtilTextMiddle.display(null, C.cGreen + "Waiting for players...", 0, 40, 10, UtilServer.getPlayers());
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		if (_manager.getGame() != null)
		{
			return;
		}

		Player player = event.getPlayer();

		joinLobby(player);
	}

	public void joinLobby(Player player)
	{
		NanoPlayer.clear(_manager, player);
		NanoPlayer.setSpectating(player, false);

		player.teleport(_spawn);
		_manager.getToHubManager().giveItems(player);
	}

	public void ensureInLobby()
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			if (player.getWorld().equals(_mineplexWorld.getWorld()))
			{
				continue;
			}

			player.sendMessage(F.main(_manager.getName(), "Not enough players to start. Returning you to the lobby."));
			joinLobby(player);
		}
	}

	public MineplexWorld getMineplexWorld()
	{
		return _mineplexWorld;
	}

	public Location getSpawn()
	{
		return _spawn;
	}
}
