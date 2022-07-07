package mineplex.core.antihack.banwave;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.gson.JsonObject;
import com.mineplex.anticheat.checks.Check;
import com.mineplex.anticheat.checks.CheckManager;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.logging.AntihackLogger;
import mineplex.core.antihack.redisnotifications.GwenBanwaveNotification;
import mineplex.core.common.util.UtilServer;

@ReflectivelyCreateMiniPlugin
public class BanWaveManager extends MiniPlugin
{
	private final BanWaveRepository _repository = new BanWaveRepository();
	private final CoreClientManager _clientManager = require(CoreClientManager.class);

	private BanWaveManager()
	{
		super("BanWaveManager");
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		runAsync(() ->
		{
			CoreClient client = require(CoreClientManager.class).Get(event.getPlayer());

			_repository.getPendingBanWaveInfo(client.getAccountId(), info ->
			{
				long now = System.currentTimeMillis();

				if (info.getTimeToBan() < now)
				{
					require(AntiHack.class).doBanWave(event.getPlayer(), info);
					_repository.flagDone(info);
				}
			});
		});
	}

	public void insertBanWaveInfo(Player player, long timeToBan, Class<? extends Check> checkClass, int vl, String server)
	{
		insertBanWaveInfo(player, timeToBan, checkClass, vl, server, null);
	}

	public void insertBanWaveInfo(Player player, long timeToBan, Class<? extends Check> checkClass, int vl, String server, Runnable after)
	{
		runAsync(() ->
		{
			String id = AntiHack.generateId();
			String newMessage = "[GWEN Cheat Detection]\n\nToken:  " + id;

			CoreClient client = _clientManager.Get(player);

			if (_repository.insertBanWaveInfo(client.getAccountId(), timeToBan, CheckManager.getCheckSimpleName(checkClass), newMessage, vl, server))
			{
				runAsync(() ->
						new GwenBanwaveNotification(UtilServer.getServerName(), player.getName(), player.getUniqueId().toString(), client.getPrimaryGroup().name().toLowerCase(), CheckManager.getCheckSimpleName(checkClass), id, timeToBan).publish());

				JsonObject custom = new JsonObject();
				custom.addProperty("is-banwave", true);

				require(AntihackLogger.class).saveMetadata(player, id, after, custom);
			}
		});
	}

	public void flagDone(CoreClient client)
	{
		_repository.flagDone(client.getAccountId());
	}
}