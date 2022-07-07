package mineplex.core.game.rejoin;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.GameDisplay;
import mineplex.serverdata.redis.RedisDataRepository;

public class GameRejoinManager implements Listener
{

	private static final int KEY_TIMEOUT = 300;

	private final MiniPlugin _parent;
	private final CoreClientManager _clientManager;
	private final RedisDataRepository<GameRejoinData> _repository;

	public GameRejoinManager(MiniPlugin parent)
	{
		_parent = parent;
		_clientManager = Managers.require(CoreClientManager.class);
		_repository = new RedisDataRepository<>(UtilServer.getRegion(), GameRejoinData.class, "gameRejoin");
	}

	public void searchToRejoin()
	{
		UtilServer.RegisterEvents(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		String id = String.valueOf(_clientManager.getAccountId(player));

		_parent.runAsync(() ->
		{
			GameRejoinData rejoinData = _repository.getElement(id);

			if (rejoinData == null)
			{
				return;
			}
			
			GameDisplay game = GameDisplay.getById(rejoinData.getGameId());

			if (game == null)
			{
				return;
			}

			BaseComponent[] text = TextComponent.fromLegacyText("\n   " + C.cYellowB + C.Line + "CLICK HERE" + C.cAqua + " to rejoin your last game of " + C.cYellowB + game.getLobbyName() + C.cAqua + "!\n");
			HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]
					{
							new TextComponent(C.cGray + "Click to join " + C.cGold + rejoinData.getServer() + C.cGray + ".")
					});
			ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + rejoinData.getServer());

			for (BaseComponent component : text)
			{
				component.setHoverEvent(hoverEvent);
				component.setClickEvent(clickEvent);
			}

			player.spigot().sendMessage(text);

			_repository.removeElement(id);
		});
	}

	public void saveRejoinData(Player player, int gameId)
	{
		int accountId = _clientManager.getAccountId(player);

		_parent.runAsync(() -> _repository.addElement(new GameRejoinData(accountId, gameId, UtilServer.getServerName()), KEY_TIMEOUT));
	}
}
