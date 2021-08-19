package mineplex.core.giveaway;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.giveaway.redis.GiveawayMessage;
import mineplex.core.giveaway.redis.GiveawayMessageHandler;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerCommandManager;

public class GiveawayManager extends MiniDbClientPlugin<PlayerGiveawayData>
{
	private CoreClientManager _clientManager;
	private ServerStatusManager _statusManager;
	private GiveawayRepository _repository;
	private HashMap<String, Giveaway> _giveawayMap;
	private HashMap<String, GiveawayCooldown> _cooldownMap;

	public GiveawayManager(JavaPlugin plugin, CoreClientManager clientManager, ServerStatusManager statusManager)
	{
		super("Giveaway Manager", plugin, clientManager);
		_clientManager = clientManager;
		_statusManager = statusManager;
		_repository = new GiveawayRepository(plugin);
		_giveawayMap = _repository.loadGiveaways();
		_cooldownMap = _repository.loadCooldowns();

		ServerCommandManager.getInstance().registerCommandType("GiveawayMessage", GiveawayMessage.class, new GiveawayMessageHandler());
	}

	public void attemptToGiveaway(final String giveawayName, final String cooldownName, final Player player, final Callback<GiveawayResponse> callback)
	{
		final int accountId = _clientManager.getAccountId(player);

		if (accountId == -1)
		{
			callback.run(new GiveawayResponse(GiveawayResponse.FailReason.INVALID_ACCOUNT_ID));
			return;
		}

		if (!hasGiveaway(giveawayName))
		{
			callback.run(new GiveawayResponse(GiveawayResponse.FailReason.INVALID_GIVEAWAY));
			return;
		}

		if (!hasCooldown(cooldownName))
		{
			callback.run(new GiveawayResponse(GiveawayResponse.FailReason.INVALID_COOLDOWN));
			return;
		}

		final Giveaway giveaway = _giveawayMap.get(giveawayName);
		final GiveawayCooldown cooldown = _cooldownMap.get(cooldownName);
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				final GiveawayResponse response;

				if (_repository.canGiveaway(accountId, giveawayName, cooldownName))
				{
					UUID uuid = UUID.randomUUID();
					if (_repository.addGiveaway(accountId, giveaway.getId(), cooldown.getId(), _statusManager.getRegion(), _statusManager.getCurrentServerName(), uuid))
					{
						response = new GiveawayResponse(uuid);
					}
					else
					{
						response = new GiveawayResponse(GiveawayResponse.FailReason.QUERY_FAILED);
					}
				}
				else
				{
					response = new GiveawayResponse(GiveawayResponse.FailReason.CANNOT_GIVEAWAY);
				}

				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (response.isSuccess())
							{
								Get(player).addGiveawayReward(new GiveawayReward(giveaway.getPrettyName(), response.getGiveawayId().toString().replace("-", "")));
								notifyPlayer(player);

								GiveawayMessage message = new GiveawayMessage(giveawayName, player.getName(), giveaway.getMessage(), giveaway.getHeader());
								message.publish();
							}
						}
						catch (Exception e)
						{

						}

						if (callback != null) callback.run(response);
					}
				});
			}
		});
	}

	public boolean hasGiveaway(String name)
	{
		return _giveawayMap.containsKey(name);
	}

	public boolean hasCooldown(String name)
	{
		return _cooldownMap.containsKey(name);
	}

	@EventHandler
	public void notifyGiveaway(UpdateEvent event)
	{
		if (event.getType() == UpdateType.MIN_01)
		{
			for (Player player : UtilServer.getPlayers())
				notifyPlayer(player);
		}
	}

	@EventHandler
	public void join(PlayerJoinEvent event)
	{
		notifyPlayer(event.getPlayer());
	}

	public void notifyPlayer(Player player)
	{
		PlayerGiveawayData data = Get(player);
		if (!data.getGiveawayRewards().isEmpty())
		{
			GiveawayReward reward = data.getGiveawayRewards().poll();
			UtilPlayer.message(player, F.main("Giveaway", "You have a prize to claim!"));
			UtilPlayer.message(player, F.main("Giveaway", "You have won " + F.elem(reward.getName())));
			UtilPlayer.message(player, F.main("Giveaway", "To claim your reward please take a screenshot and contact support"));
			UtilPlayer.message(player, F.main("Giveaway", "Reward Key: " + F.elem(reward.getUuid())));
		}
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(uuid, _repository.loadPlayerGiveaway(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT g.prettyName, ag.uuid FROM accountGiveaway AS ag INNER JOIN giveaway AS g ON ag.giveawayId = g.id WHERE ag.claimed = 0 AND ag.accountId = " + accountId + ";";
	}

	@Override
	protected PlayerGiveawayData addPlayer(UUID uuid)
	{
		return new PlayerGiveawayData();
	}
}
