package mineplex.core.rankGiveaway.titangiveaway;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.rankGiveaway.redis.GiveawayMessageHandler;
import mineplex.core.rankGiveaway.redis.TitanChestGiveawayHandler;
import mineplex.core.rankGiveaway.redis.TitanChestGiveawayMessage;
import mineplex.core.rankGiveaway.redis.TitanGiveawayMessage;
import mineplex.core.status.ServerStatusManager;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerCommandManager;

public class TitanGiveawayManager extends MiniPlugin
{
	private static final double RANK_FIND_CHANCE = 0.001;
	
	private final PermissionGroup _group;
	
	private TitanGiveawayRepository _repository;
	private CoreClientManager _clientManager;
	private ServerStatusManager _statusManager;
	private Random _random;

	public TitanGiveawayManager(JavaPlugin plugin, CoreClientManager clientManager, ServerStatusManager statusManager)
	{
		super("Titan Giveaway", plugin);

		_repository = new TitanGiveawayRepository(plugin);
		_clientManager = clientManager;
		_statusManager = statusManager;
		_random = new Random();

		_group = PermissionGroup.TITAN;

		ServerCommandManager.getInstance().registerCommandType("TitanGiveawayMessage", TitanGiveawayMessage.class, new GiveawayMessageHandler(plugin));
		ServerCommandManager.getInstance().registerCommandType("TitanChestGiveawayMessage", TitanChestGiveawayMessage.class, new TitanChestGiveawayHandler(_statusManager));
	}

	public void openPumpkin(final Player player, final Runnable onSuccess)
	{
		double rand = _random.nextDouble();
		if (!hasTitan(player) && rand < RANK_FIND_CHANCE)
		{
			final int accountId = _clientManager.getAccountId(player);
			final Region region = getRegion();
			final String serverName = getServerName();

			// Need to check database that we can give away a rank
			runAsync(() ->
			{
				final boolean pass = _repository.canGiveaway(region);

				if (pass && _repository.addTitan(accountId, region, serverName))
				{
					runSync(() ->
					{
						giveRank(player, () ->
						{
							TitanGiveawayMessage message = new TitanGiveawayMessage(player.getName(), _repository.getTitanCount() + 1);
							message.publish();
							if (onSuccess != null)
							{
								onSuccess.run();
							}
						});

					});
				}
			});
		}
	}

	/**
	 * Confirm that the player doesn't already have TITAN rank
	 */
	private boolean hasTitan(Player player)
	{
		return _clientManager.Get(player).getPrimaryGroup().inheritsFrom(PermissionGroup.TITAN);
	}

	public Region getRegion()
	{
		return _statusManager.getRegion();
	}

	public String getServerName()
	{
		return _statusManager.getCurrentServerName();
	}

	private void giveRank(Player player, Runnable after)
	{
		_clientManager.setPrimaryGroup(player, _group, after);
	}
}