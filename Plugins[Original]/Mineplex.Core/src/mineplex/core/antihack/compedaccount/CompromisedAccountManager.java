package mineplex.core.antihack.compedaccount;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.gson.JsonObject;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.api.ApiHost;
import mineplex.core.common.api.ApiWebCall;
import mineplex.core.common.util.UtilPlayer;

@ReflectivelyCreateMiniPlugin
public class CompromisedAccountManager extends MiniPlugin
{
	private final CompromisedAccountRepository _repository = new CompromisedAccountRepository();

	private final CoreClientManager _clientManager = require(CoreClientManager.class);

	private final ApiWebCall _apiCall;

	private CompromisedAccountManager()
	{
		super("CompedAccount");

		String url = "http://" + ApiHost.getBanner().getHost() + ":" + ApiHost.getBanner().getPort() + "/";
		_apiCall = new ApiWebCall(url);
	}

	public void submitImmediateBan(Player player)
	{
		_repository.insertPunishment(_clientManager.getAccountId(player), UtilPlayer.getIp(player), "immediate").whenComplete((res, err) ->
		{
			if (err != null)
			{
				getPlugin().getLogger().log(Level.SEVERE, "An unexpected error occurred while submitting immediate ban of " + player.getName(), err);
			}
		});
	}

	public void submitPendingDelayedBan(Player player)
	{
		_repository.insertPunishment(_clientManager.getAccountId(player), UtilPlayer.getIp(player), "predelayed").whenComplete((res, err) ->
		{
			if (err != null)
			{
				getPlugin().getLogger().log(Level.SEVERE, "An unexpected error occurred while submitting delayed ban of " + player.getName(), err);
			}
		});
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		runAsync(() ->
		{
			JsonObject response = _apiCall.post("api/server/login/" + event.getPlayer().getName(), JsonObject.class, getPlayerInfo(event.getPlayer()));
			if (response != null && response.get("error") != null)
			{
				getPlugin().getLogger().log(Level.SEVERE, "Response from Banner: " + response);
			}
		});
	}

	public void triggerPriorityBan(Player player, PriorityCause cause)
	{
		runAsync(() ->
		{
			JsonObject response = _apiCall.post("api/banner/trigger/" + player.getName(), JsonObject.class, new TriggerPriorityInfo(getPlayerInfo(player), cause));
			if (response != null && response.get("error") != null)
			{
				getPlugin().getLogger().log(Level.SEVERE, "Response from Banner: " + response);
			}
		});
	}

	private PlayerInfo getPlayerInfo(Player player)
	{
		CoreClient coreClient = _clientManager.Get(player);
		return new PlayerInfo(
				player.getName(),
				coreClient.getName(),
				player.getUniqueId(),
				coreClient.getAccountId(),
				UtilPlayer.getIp(player)
		);
	}
}
